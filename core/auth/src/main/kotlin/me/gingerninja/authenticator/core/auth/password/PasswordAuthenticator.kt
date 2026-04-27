package me.gingerninja.authenticator.core.auth.password

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import me.gingerninja.authenticator.core.auth.Authenticator
import me.gingerninja.authenticator.core.auth.Crypto
import me.gingerninja.authenticator.core.auth.EnableAuthenticator
import me.gingerninja.authenticator.core.auth.UpdatableAuthenticator
import me.gingerninja.authenticator.core.auth.biometric.BiometricKeyHandler
import me.gingerninja.authenticator.core.common.Dispatcher
import me.gingerninja.authenticator.core.common.DispatcherType
import me.gingerninja.authenticator.core.common.toDatabaseByteArray
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordAuthenticator @Inject internal constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(DispatcherType.IO) private val dispatcher: CoroutineDispatcher,
    private val settings: NinjAuthSettings,
    private val dbAuthenticator: NinjAuthDatabaseAuthenticator,
    private val biometricKeyHandler: BiometricKeyHandler,
) : Authenticator<PasswordAuthenticator.AuthConfig>(
    settings,
    dbAuthenticator
),
    EnableAuthenticator<PasswordAuthenticator.EnableConfig, PasswordAuthenticator.DisableConfig>,
    UpdatableAuthenticator<PasswordAuthenticator.UpdateConfig> {
    /* TODO MasterKey.Builder(context, "baseKey")
            .setUserAuthenticationRequired(false)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()*/

    override suspend fun enable(config: EnableConfig) {
        require(config.type != SecurityConfig.LockType.NONE)

        dbAuthenticator.openDatabase(defaultPassBytes)

        val masterKey = crypto.generateKey()
        val masterCipher = crypto.getCipher(Crypto.CipherWrite.ENCRYPT, masterKey, false)

        val pass = crypto.generateDbPass()
        try {
            val encryptedPass = crypto.encrypt(masterCipher, pass)

            settings.setSecurityWithDatabasePass(config.type, encryptedPass)

            PasswordKeyHandler(context, dispatcher).use {
                // TODO this is the weakest point as the database contains the raw master key and the password may be just 4 digits
                it.create(config.password)
                it.setMasterKey(masterKey.encoded)
            }
            dbAuthenticator.changePassword(pass)

        } finally {
            pass.fill(0)
            destroyKey(masterKey)
        }
    }

    @Throws(PasswordAuthException::class)
    override suspend fun authenticate(config: AuthConfig) {
        authenticate(config.password)
    }

    @Throws(PasswordAuthException::class)
    override suspend fun update(config: UpdateConfig) {
        PasswordKeyHandler(context, dispatcher).use {
            it.authenticate(config.oldPassword)
            it.changePassword(config.newPassword)
        }
    }

    @Throws(PasswordAuthException::class)
    override suspend fun disable(config: DisableConfig) {
        val encryptedPass = settings.getEncryptedDatabasePass() ?: return

        val passwordKeyHandler = PasswordKeyHandler(context, dispatcher)

        // intentionally not using the .use {...} syntax as we call delete on it later
        val masterKey = passwordKeyHandler.let {
            it.authenticate(config.password)
            it.getMasterKey().asSecretKey
        }

        val cipher = crypto.getCipher(Crypto.CipherRead.DECRYPT, masterKey, encryptedPass)
        val decrypted = crypto.decrypt(cipher, encryptedPass)

        try {
            // dbAuthenticator.close() // TODO do we want to close the DB?
            dbAuthenticator.openDatabase(decrypted)
            dbAuthenticator.changePassword(defaultPassBytes) // TODO is this good? do we want to change the password of the main database?

            biometricKeyHandler.remove()
            settings.setSecurityWithDatabasePass(SecurityConfig.LockType.NONE, null)

            passwordKeyHandler.delete()
        } finally {
            decrypted.fill(0)
            destroyKey(masterKey)
        }
    }

    private suspend fun authenticate(password: ByteArray) {
        when (settings.data.first().security.lockType) {
            SecurityConfig.LockType.NONE -> throw RuntimeException("No lock")
            SecurityConfig.LockType.PIN -> authenticatePIN(password)
            SecurityConfig.LockType.PASSWORD -> authenticatePassword(password)
        }
    }

    private suspend fun authenticatePIN(password: ByteArray) {
        authenticateLegacy(password)
    }

    private suspend fun authenticatePassword(password: ByteArray) {
        authenticateLegacy(password)
    }

    private suspend fun authenticateLegacy(password: ByteArray) {
        val key = PasswordKeyHandler(context, dispatcher).use {
            it.authenticate(password)
            it.getMasterKey()
        }

        unlockDatabase(key)
    }

    class EnableConfig(
        internal val password: ByteArray,
        internal val type: SecurityConfig.LockType
    )

    class DisableConfig(
        internal val password: ByteArray
    )

    class AuthConfig(
        internal val password: ByteArray
    )

    class UpdateConfig(
        internal val oldPassword: ByteArray,
        internal val newPassword: ByteArray
    )

    companion object {
        private const val DEFAULT_PASS = "fakepass"

        val defaultPassBytes: ByteArray get() = DEFAULT_PASS.toDatabaseByteArray()
    }
}