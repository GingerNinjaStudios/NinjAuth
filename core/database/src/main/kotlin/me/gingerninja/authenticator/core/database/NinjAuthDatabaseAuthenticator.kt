package me.gingerninja.authenticator.core.database

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NinjAuthDatabaseAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val internalDatabase = MutableStateFlow<NinjAuthDatabase?>(null)

    internal val database: StateFlow<NinjAuthDatabase?> = internalDatabase.asStateFlow()

    internal val isOpen = database.map { it != null && it.isOpen }

    fun openDatabaseDefault() {
        openDatabase(SQLiteDatabase.getBytes("fakepass".toCharArray()))
    }

    fun openDatabase(passphrase: ByteArray, clearPassphrase: Boolean = true) {
        //SQLiteDatabase.loadLibs(context)

        if (internalDatabase.value?.isOpen == true) {
            return
        }

        internalDatabase.value?.close()

        val factory = SupportFactory(passphrase)

        if (clearPassphrase) {
            passphrase.fill(0)
        }

        val db = Room
            .databaseBuilder(
                context,
                NinjAuthDatabase::class.java,
                "ninjauth"
            )
            .openHelperFactory(factory)
            .build()

        internalDatabase.value = db

        db.query("", arrayOf())
    }

    fun close() {
        internalDatabase.update {
            it?.close()

            null
        }
    }
}