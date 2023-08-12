package me.gingerninja.authenticator.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.database.dao.AccountDao
import me.gingerninja.authenticator.core.database.model.AccountEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

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
            uid = "12345",
            title = "Test #1",
            issuer = "Issuer #1",
            accountName = "Test account #1",
            secret = "N/A",
            typeSpecificData = 30
        )

        val id = accountDao.saveAccount(account)

        val inserted = accountDao.getAccountWithLabels(id)

        assertEquals("Test account", inserted.account.accountName)
    }

}