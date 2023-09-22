package me.gingerninja.authenticator.core.auth.password

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.gingerninja.authenticator.core.database.InvalidKeyPasswordException
import me.gingerninja.authenticator.core.database.LegacyKeyDatabase

internal class LegacyPasswordKeyHandler(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : AutoCloseable {
    private var db: LegacyKeyDatabase? = null

    val exists: Boolean get() = LegacyKeyDatabase.exists(context)

    suspend fun create(password: CharArray) = withContext(dispatcher) {
        delete()
        authenticate(password)
    }

    /**
     * @throws InvalidKeyPasswordException if the password is invalid
     */
    suspend fun authenticate(password: CharArray) = withContext(dispatcher) {
        require(db == null) { "DB is already open and close() was not called before" }

        db = LegacyKeyDatabase.get(context).apply { // TODO should be getIfExists instead of get
            open(password)
        }
    }

    suspend fun changePassword(password: CharArray) = withContext(dispatcher) {
        requireNotNull(db).changePassword(password)
    }

    override fun close() {
        db?.close()
        db = null
    }

    fun delete() {
        db?.apply {
            close()
            delete()
        }

        db = null
    }

    suspend fun getMasterKey(): ByteArray = withContext(dispatcher) {
        requireNotNull(db).let {
            it[LegacyKeyDatabase.MASTER_KEY_NAME].value
        }
    }

    suspend fun setMasterKey(value: ByteArray) = withContext(dispatcher) {
        requireNotNull(db).let {
            it[LegacyKeyDatabase.MASTER_KEY_NAME] = value
        }
    }
}