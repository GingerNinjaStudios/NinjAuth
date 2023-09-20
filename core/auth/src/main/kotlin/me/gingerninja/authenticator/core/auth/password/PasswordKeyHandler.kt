package me.gingerninja.authenticator.core.auth.password

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.gingerninja.authenticator.core.database.InvalidKeyPasswordException

class PasswordKeyHandler(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : AutoCloseable {
    private var legacyHandler: LegacyPasswordKeyHandler =
        LegacyPasswordKeyHandler(context, dispatcher)

    suspend fun create(password: CharArray) = withContext(dispatcher) {
        legacyHandler.create(password)
    }

    /**
     * @throws InvalidKeyPasswordException if the password is invalid
     */
    @Throws(InvalidKeyPasswordException::class)
    suspend fun authenticate(password: CharArray) = withContext(dispatcher) {
        legacyHandler.authenticate(password)
    }

    suspend fun changePassword(password: CharArray) = withContext(dispatcher) {
        legacyHandler.changePassword(password)
    }

    override fun close() {
        legacyHandler.close()
    }

    fun delete() {
        legacyHandler.delete()
    }

    suspend fun getMasterKey(): ByteArray = withContext(dispatcher) {
        legacyHandler.getMasterKey()
    }

    suspend fun setMasterKey(value: ByteArray) = withContext(dispatcher) {
        legacyHandler.setMasterKey(value)
    }
}