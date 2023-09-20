package me.gingerninja.authenticator.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.sqlcipher.database.SQLiteDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import javax.inject.Provider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseAuthenticatorTest {
    private lateinit var context: Context
    private lateinit var db: NinjAuthDatabaseAuthenticator

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()

        val provider = Provider {
            // Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java)
            Room.databaseBuilder(context, NinjAuthDatabase::class.java, "test.db")
        }

        db = NinjAuthDatabaseAuthenticator(/*context, */provider)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
        context.deleteDatabase("test.db")
    }

    @Test
    fun openDatabase_isOpenTrue() = runTest {
        db.openDatabase(SQLiteDatabase.getBytes("test".toCharArray()))
        assertTrue(db.isOpen.first())
    }

    @Test
    fun openDatabase_isOpenFalse() = runTest {
        assertFalse(db.isOpen.first())
    }

    @Test
    fun changeDatabasePassword_rightPass() = runTest {
        db.apply {
            openDatabase(SQLiteDatabase.getBytes("test".toCharArray()))
            changePassword("newpass".toCharArray())
            database.first()!!.accountDao().saveAccount(testTotpAccountsWithId[0])
            close()
        }

        db.openDatabase(SQLiteDatabase.getBytes("newpass".toCharArray()))
        val accounts = db.database.first()!!.accountDao().getAccounts().first()

        assertEquals(1, accounts.size)
    }

    @Test(expected = InvalidDatabasePassword::class)
    fun changeDatabasePassword_wrongPass() = runTest {
        db.apply {
            openDatabase(SQLiteDatabase.getBytes("test".toCharArray()))
            changePassword("newpass".toCharArray())
            database.first()!!.accountDao().saveAccount(testTotpAccountsWithId[0])
            close()
        }

        db.openDatabase(SQLiteDatabase.getBytes("wrongpass".toCharArray()))
    }
}