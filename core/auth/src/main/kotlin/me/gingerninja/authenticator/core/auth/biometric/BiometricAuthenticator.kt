package me.gingerninja.authenticator.core.auth.biometric

import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import me.gingerninja.authenticator.core.auth.Authenticator
import me.gingerninja.authenticator.core.auth.Crypto
import me.gingerninja.authenticator.core.auth.password.PasswordKeyHandler
import me.gingerninja.authenticator.core.common.Dispatcher
import me.gingerninja.authenticator.core.common.DispatcherType
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKey
import javax.inject.Inject

class BiometricAuthenticator @Inject internal constructor(
    @ApplicationContext context: Context,
    @Dispatcher(DispatcherType.IO) dispatcher: CoroutineDispatcher,
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator,
    private val biometricKeyHandler: BiometricKeyHandler,
) : Authenticator<BiometricAuthenticator.EnableConfig, Unit, BiometricAuthenticator.AuthConfig, Nothing>(
    settings,
    dbAuthenticator
) {
    private val passwordKeyHandler: PasswordKeyHandler = PasswordKeyHandler(context, dispatcher)

    private val keyguardManager by lazy {
        context.getSystemService(KeyguardManager::class.java)
    }

    private val isDeviceSecure: Boolean get() = keyguardManager.isDeviceSecure

    val isAvailable: Boolean get() = biometricKeyHandler.status == BiometricKeyHandler.Status.AVAILABLE

    suspend fun canAuthenticate(): Boolean {
        return biometricKeyHandler.status == BiometricKeyHandler.Status.AVAILABLE &&
                settings.data.first().security.isBiometricsEnabled
    }

    override suspend fun enable(config: EnableConfig) {
        var masterKey: SecretKey? = null
        try {
            masterKey = passwordKeyHandler.use {
                it.authenticate(config.password)
                it.getMasterKey()
            }.asSecretKey

            try {
                disable()
            } catch (e: Throwable) {
                // TODO error handling
            }

            try {
                val wrappedBioKey = biometricKeyHandler.createKey()
                    ?.let { key ->
                        // get the cipher for wrapping the new biometric key
                        val cipher = crypto.getCipher(Crypto.CipherWrite.WRAP, key, true)

                        // wrap the cipher with biometrics
                        val bioCipher =
                            biometricKeyHandler.authenticate(config.activity, cipher).await()

                        // wrap the master key with the biometrics-enabled cipher
                        crypto.wrapKeyAndEncode(masterKey, bioCipher)
                    } ?: return

                // save the wrapped biometric key
                settings.apply {
                    setBiometricVersion(SECURITY_BIO_VERSION)
                    setBiometricKey(wrappedBioKey)
                }
            } catch (e2: InvalidAlgorithmParameterException) {
                val bioResults = biometricKeyHandler.status

                if (bioResults == BiometricKeyHandler.Status.NO_BIOMETRICS || !isDeviceSecure) {
                    throw BiometricException(
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
                        e2.message
                    )
                } else {
                    throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e2.message)
                }
            } catch (e: KeyPermanentlyInvalidatedException) {
                //Timber.e(e, "Cannot use bio key")
                disable()
                throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
            } catch (e: IllegalBlockSizeException) {
                //Timber.e(e, "Cannot use bio key")
                disable()
                throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
            }
        } finally {
            destroyKey(masterKey)
        }
    }

    override suspend fun authenticate(config: AuthConfig) {
        var bioKey: SecretKey? = null
        try {
            bioKey = biometricKeyHandler.getKey()

            val masterWrapped = settings.getBiometricKey() ?: return

            val cipher = crypto.getCipher(Crypto.CipherRead.UNWRAP, bioKey, masterWrapped).let {
                biometricKeyHandler.authenticate(config.activity, it).await()
            }

            crypto.unwrapKey(masterWrapped, cipher).apply {
                unlockDatabase(this)
            }
        } catch (e: InvalidKeyException) {
            biometricKeyHandler.remove()
            throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
        } catch (e: UnrecoverableKeyException) {
            biometricKeyHandler.remove()
            throw BiometricException(BiometricException.ERROR_KEY_INVALIDATED, e.message)
        } finally {
            destroyKey(bioKey)
        }
    }

    override suspend fun disable(config: Unit) {
        biometricKeyHandler.remove()
    }

    suspend fun disable() = disable(Unit)

    class EnableConfig(internal val activity: FragmentActivity, internal val password: CharArray)
    class AuthConfig(internal val activity: FragmentActivity)

    companion object {
        private const val SECURITY_BIO_VERSION = 1
    }
}