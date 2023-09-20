package me.gingerninja.authenticator.core.auth

import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.DestroyFailedException
import javax.security.auth.Destroyable

abstract class Authenticator<EnableConfig, DisableConfig, AuthConfig, UpdateConfig>(
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator
) {
    protected val crypto: Crypto = Crypto()

    abstract suspend fun enable(config: EnableConfig)

    abstract suspend fun authenticate(config: AuthConfig)

    open suspend fun update(config: UpdateConfig) {
        throw UnsupportedOperationException()
    }

    abstract suspend fun disable(config: DisableConfig)

    protected val ByteArray.asSecretKey: SecretKey get() = SecretKeySpec(this, "AES")

    internal suspend fun unlockDatabase(key: ByteArray, destroyKey: Boolean = true) {
        try {
            unlockDatabase(
                masterKey = key.asSecretKey,
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
                destroyKey(masterKey)
            }
        }
    }

    protected fun destroyKey(key: SecretKey?) {
        if (key != null && Destroyable::class.java.isAssignableFrom(key.javaClass) && !key.isDestroyed) {
            try {
                key.destroy()
            } catch (ignored: DestroyFailedException) {
            }
        }
    }
}
