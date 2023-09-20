package me.gingerninja.authenticator.core.database

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class NinjAuthDatabaseAuthenticator @Inject constructor(
    //@ApplicationContext private val context: Context,
    private val dbBuilder: Provider<RoomDatabase.Builder<NinjAuthDatabase>>
) {
    private val internalDatabase = MutableStateFlow<NinjAuthDatabase?>(null)

    internal val database: StateFlow<NinjAuthDatabase?> = internalDatabase.asStateFlow()

    internal val isOpen = database.map { it != null && it.isOpen }

    fun openDatabase(passphrase: ByteArray, clearPassphrase: Boolean = true) {
        //SQLiteDatabase.loadLibs(context)

        if (internalDatabase.value?.isOpen == true) {
            return
        }

        internalDatabase.value?.close()

        val factory = SupportFactory(passphrase, null, clearPassphrase)

        val db = dbBuilder.get()
            .openHelperFactory(factory)
            .build()
            .apply {
                try {
                    // force opening the database
                    openHelper.writableDatabase
                } catch (e: SQLiteException) {
                    throw InvalidDatabasePassword(e)
                }
            }

        /*val db = Room
            .databaseBuilder(
                context,
                NinjAuthDatabase::class.java,
                "ninjauth.db"
            )
            .openHelperFactory(factory)
            .build()*/

        internalDatabase.value = db
    }

    fun changePassword(charArray: CharArray) {
        val dbOjb =
            internalDatabase.value ?: throw IllegalStateException("The database is not open")

        // as we created the database with the SQL Cipher factory, we can retrieve the encrypted DB
        val db = dbOjb.openHelper.writableDatabase as SQLiteDatabase
        db.changePassword(charArray)

        charArray.fill(0.toChar())
    }

    fun close() {
        internalDatabase.update {
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