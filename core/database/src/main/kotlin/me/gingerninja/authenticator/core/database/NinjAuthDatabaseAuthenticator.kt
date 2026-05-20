package me.gingerninja.authenticator.core.database

import android.database.sqlite.SQLiteException
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NinjAuthDatabaseAuthenticator @Inject constructor(
    private val dbBuilder: Provider<RoomDatabase.Builder<NinjAuthDatabase>>
) {
    init {
        System.loadLibrary("sqlcipher")
    }

    internal val database: StateFlow<NinjAuthDatabase?>
        field = MutableStateFlow<NinjAuthDatabase?>(null)

    internal val isOpen = database.map { it != null && it.isOpen }

    fun openDatabase(passphrase: ByteArray, clearPassphrase: Boolean = true) {
        if (database.value?.isOpen == true) {
            return
        }

        database.value?.close()

        val factory = SupportOpenHelperFactory(passphrase)

        val db = dbBuilder.get()
            .openHelperFactory(factory)
            .build()
            .apply {
                try {
                    // force opening the database
                    openHelper.writableDatabase

                    if (clearPassphrase) {
                        for (i in passphrase.indices) {
                            passphrase[i] = 0
                        }
                    }
                } catch (e: SQLiteException) {
                    throw InvalidDatabasePassword(e)
                }
            }

        database.value = db
    }

    fun changePassword(password: ByteArray) {
        val dbOjb =
            database.value ?: throw IllegalStateException("The database is not open")

        // as we created the database with the SQL Cipher factory, we can retrieve the encrypted DB
        val db = dbOjb.openHelper.writableDatabase as SQLiteDatabase
        db.changePassword(password)

        password.fill(0)
    }

    fun close() {
        database.update {
            it?.close()

            null
        }
    }

    /*companion object {
        @VisibleForTesting
        fun inMemoryInstance(context: Context) = NinjAuthDatabaseAuthenticator(
            context
        ) { Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java) }
    }*/
}

class InvalidDatabasePassword(cause: Throwable) :
    Exception("The database is not readable, likely wrong password was provided", cause)