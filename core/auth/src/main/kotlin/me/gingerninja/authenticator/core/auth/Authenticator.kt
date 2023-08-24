package me.gingerninja.authenticator.core.auth

import android.app.KeyguardManager
import android.content.Context
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import me.gingerninja.authenticator.core.common.Dispatcher
import me.gingerninja.authenticator.core.common.DispatcherType
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Authenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator,
    private val biometricAuthenticator: BiometricAuthenticator,
    @Dispatcher(DispatcherType.IO) private val dispatcher: CoroutineDispatcher,
) {
    private val crypto = Crypto()

    private val keyguardManager by lazy {
        context.getSystemService(KeyguardManager::class.java)
    }

    private val isDeviceSecure: Boolean get() = keyguardManager.isDeviceSecure

    suspend fun canBiometricAuthenticate() {
        biometricAuthenticator.status == BiometricAuthenticator.Status.AVAILABLE &&
                settings.data.first().security.isBiometricsEnabled
    }

    suspend fun create(method: AuthenticationMethod) {

    }

    suspend fun authenticate(method: AuthenticationMethod) {
        when (method) {
            is AuthenticationMethod.Biometric -> authenticate(method.activity)
            is AuthenticationMethod.Input -> authenticate(method.value)
        }
    }

    private suspend fun authenticate(activity: FragmentActivity) {
        var bioKey: SecretKey? = null
        try {
            bioKey = biometricAuthenticator.getKey()

            val masterWrapped = settings.getBiometricKey() ?: return

            val cipher = crypto.getCipher(Crypto.CipherRead.UNWRAP, bioKey, masterWrapped).let {
                biometricAuthenticator.authenticate(activity, it).await()
            }

            crypto.unwrapKey(masterWrapped, cipher).apply {
                unlockDatabase(this)
            }
        } catch (e: InvalidKeyException) {
            biometricAuthenticator.remove()
            throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
        } catch (e: UnrecoverableKeyException) {
            biometricAuthenticator.remove()
            throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
        } finally {
            bioKey?.destroy()
        }
    }

    private suspend fun authenticate(password: CharArray) {
        when (settings.data.first().security.lockType) {
            SecurityConfig.LockType.NONE -> throw RuntimeException("No lock")
            SecurityConfig.LockType.PIN -> authenticatePIN(password)
            SecurityConfig.LockType.PASSWORD -> authenticatePassword(password)
        }
    }

    private suspend fun authenticatePIN(password: CharArray) {
        authenticateLegacy(password)
    }

    private suspend fun authenticatePassword(password: CharArray) {
        authenticateLegacy(password)
    }

    private suspend fun authenticateLegacy(password: CharArray) {
        val key = LegacyKeyHandler(context, dispatcher).use {
            it.authenticate(password)
            it.getMasterKey()
        }

        unlockDatabase(key)
    }

    private suspend fun unlockDatabase(key: ByteArray, destroyKey: Boolean = true) {
        try {
            unlockDatabase(
                masterKey = SecretKeySpec(key, "AES"),
                destroyKey = destroyKey
            )
        } finally {
            key.fill(0)
        }
    }

    private suspend fun unlockDatabase(masterKey: SecretKey, destroyKey: Boolean = true) {
        try {
            val encryptedDbPass = settings.getEncryptedDatabasePass() ?: return

            crypto.getCipher(Crypto.CipherRead.DECRYPT, masterKey, encryptedDbPass)
                .let { cipher ->
                    crypto.decrypt(cipher, encryptedDbPass)
                }
                .let {
                    dbAuthenticator.openDatabase(it)
                    it.fill(0)
                }
        } finally {
            if (destroyKey) {
                masterKey.destroy()
            }
        }
    }
}

sealed interface AuthenticationMethod {
    class Input(internal val value: CharArray, internal val type: SecurityConfig.LockType? = null) : AuthenticationMethod
    class Biometric(internal val activity: FragmentActivity) : AuthenticationMethod
}