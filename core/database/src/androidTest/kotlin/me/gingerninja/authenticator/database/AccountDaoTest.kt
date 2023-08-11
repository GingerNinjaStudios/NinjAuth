package me.gingerninja.authenticator.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.database.dao.AccountDao
import me.gingerninja.authenticator.database.model.AccountEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.UUID
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class AccountDaoTest {
    private lateinit var accountDao: AccountDao
    private lateinit var db: NinjAuthDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java).build()
        accountDao = db.accountDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun createAccount() = runTest {
        val account = AccountEntity(
            id = 0,
            uid = UUID.randomUUID().toString(),
            accountName = "Test account",
            secret = "N/A",
            typeSpecificData = 30
        )

        val id = accountDao.saveAccount(account)

        val inserted = accountDao.getAccountWithLabels(id)

        assertEquals("Test account", inserted.account.accountName)
    }

}