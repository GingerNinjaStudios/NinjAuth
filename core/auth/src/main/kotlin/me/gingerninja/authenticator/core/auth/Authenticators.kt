package me.gingerninja.authenticator.core.auth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

sealed interface Authenticator2<EnableConfig> {
    suspend fun enable(config: EnableConfig)

    suspend fun authenticate(config: EnableConfig)

    suspend fun disable()
}

abstract class InputAuthenticator : Authenticator2<InputAuthenticator.Config> {
    class Config(internal val value: CharArray, internal val type: SecurityConfig.LockType? = null)
}

abstract class AuthFactory<T : Authenticator2<*>> internal constructor(
    protected val settings: NinjAuthSettings,
    protected val dbAuthenticator: NinjAuthDatabaseAuthenticator,
    protected val crypto: Crypto,
) {
    abstract fun createAuthenticator(): T

    internal suspend fun unlockDatabase(key: ByteArray, destroyKey: Boolean = true) {
        try {
            unlockDatabase(
                masterKey = SecretKeySpec(key, "AES"),
                destroyKey = destroyKey
            )
        } finally {
            key.fill(0)
        }
    }

    internal suspend fun unlockDatabase(masterKey: SecretKey, destroyKey: Boolean = true) {
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

@Singleton
class BiometricAuthFactory @Inject constructor(
    settings: NinjAuthSettings,
    dbAuthenticator: NinjAuthDatabaseAuthenticator,
    private val biometricAuthenticator: BiometricAuthenticator,
) : AuthFactory<BiometricAuthenticator2>(
    settings = settings,
    dbAuthenticator = dbAuthenticator,
    crypto = Crypto()
) {
    override fun createAuthenticator(): BiometricAuthenticator2 = BiometricAuthenticator2(
        settings = settings,
        crypto = crypto,
        biometricAuthenticator = biometricAuthenticator,
        factory = this
    )
}

class BiometricAuthenticator2 internal constructor(
    private val settings: NinjAuthSettings,
    private val crypto: Crypto,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val factory: BiometricAuthFactory,
) : Authenticator2<BiometricAuthenticator2.Config> {
    override suspend fun authenticate(config: Config) {
        var bioKey: SecretKey? = null
        try {
            bioKey = biometricAuthenticator.getKey()

            val masterWrapped = settings.getBiometricKey() ?: return

            val cipher = crypto.getCipher(Crypto.CipherRead.UNWRAP, bioKey, masterWrapped).let {
                biometricAuthenticator.authenticate(config.activity, it).await()
            }

            crypto.unwrapKey(masterWrapped, cipher).apply {
                factory.unlockDatabase(this)
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

    class Config(internal val activity: FragmentActivity)

    override suspend fun enable(config: Config) {
        TODO("Not yet implemented")
    }

    override suspend fun disable() {
        TODO("Not yet implemented")
    }
}