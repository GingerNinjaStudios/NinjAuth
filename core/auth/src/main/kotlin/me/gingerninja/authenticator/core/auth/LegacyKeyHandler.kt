package me.gingerninja.authenticator.core.auth

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.gingerninja.authenticator.core.database.InvalidKeyPasswordException
import me.gingerninja.authenticator.core.database.LegacyKeyDatabase

class LegacyKeyHandler(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : AutoCloseable {
    private var db: LegacyKeyDatabase? = null

    /**
     * @throws InvalidKeyPasswordException if the password is invalid
     */
    suspend fun authenticate(password: CharArray) = withContext(dispatcher) {
        require(db == null) { "DB is already open and close() was not called before" }

        db = LegacyKeyDatabase.getIfExists(context)?.apply {
            open(password)
        }
    }

    suspend fun changePassword(password: CharArray) = withContext(dispatcher) {
        requireNotNull(db).changePassword(password)
    }

    override fun close() {
        requireNotNull(db).close()
        db = null
    }

    suspend fun getMasterKey(): ByteArray = withContext(dispatcher) {
        requireNotNull(db).let {
            it[LegacyKeyDatabase.MASTER_KEY_NAME].value
        }
    }

}