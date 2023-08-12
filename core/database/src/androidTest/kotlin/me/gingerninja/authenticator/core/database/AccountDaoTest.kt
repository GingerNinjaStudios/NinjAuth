package me.gingerninja.authenticator.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.database.dao.AccountDao
import me.gingerninja.authenticator.core.database.dao.AccountLabelDao
import me.gingerninja.authenticator.core.database.dao.LabelDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountDaoTest {
    private lateinit var accountDao: AccountDao
    private lateinit var labelDao: LabelDao
    private lateinit var accountLabelDao: AccountLabelDao
    private lateinit var db: NinjAuthDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java).build()
        accountDao = db.accountDao()
        labelDao = db.labelDao()
        accountLabelDao = db.accountLabelDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /**
     * Helper function for saving a single account.
     */
    private suspend fun saveAccount(): Long {
        val id = accountDao.saveAccount(testTotpAccounts[0])

        val inserted = accountDao.getAccountWithLabels(id)

        assertEquals(testTotpAccounts[0].accountName, inserted?.account?.accountName)

        return id
    }

    @Test
    @Throws(Exception::class)
    fun createAccount_single() = runTest {
        saveAccount()
    }

    @Test
    @Throws(Exception::class)
    fun createAccountWithMatchingUids_retry() = runTest {
        val id1 = accountDao.saveAccount(testTotpAccounts[0])
        val id2 = accountDao.saveAccount(testTotpAccounts[0])

        val inserted1 = accountDao.getAccountWithLabels(id1)
        val inserted2 = accountDao.getAccountWithLabels(id2)

        assertNotNull(inserted1, "Entity #1 was not saved")
        assertNotNull(inserted2, "Entity #2 was not saved")

        assertNotEquals(
            inserted1.account.uid,
            inserted2.account.uid,
            "The entities' UIDs are equal"
        )
    }

    @Test
    fun deleteAccount() = runTest {
        val id = saveAccount()

        accountDao.deleteAccount(id)

        val isEmpty = accountDao.getAccounts().first().isEmpty()

        assertTrue(isEmpty, "Account was not deleted")
    }

    @Test
    fun searchAccountsWithoutLabel() = runTest {
        testTotpAccounts.forEach {
            accountDao.saveAccount(it)
        }

        val count = accountDao.getAccounts().first().size

        assertEquals(testTotpAccounts.size, count)

        val results = accountDao.getAccounts(search = "ant").first()

        assertEquals(2, results.size)
    }

    @Test
    fun searchAccountsWithLabelsMatchAll() = runTest {
        val map = createLinkedEntries(accountDao, labelDao, accountLabelDao)

        val targetAccount = map.keys.first()
        val targetLabels = map[targetAccount]!!

        val targetLabelIds = targetLabels.map { it.id }.toSet()

        val results = accountDao.getAccounts(labels = targetLabelIds, matchAllLabels = true).first()

        results.forEach { (_, labels) ->
            val labelIds = labels.map { it.id }.toSet()
            val section = targetLabelIds.intersect(labelIds)

            assertEquals(targetLabelIds.size, section.size, "Didn't match all labels")
        }
    }

    @Test
    fun searchAccountsWithLabelsMatchAny() = runTest {
        val map = createLinkedEntries(accountDao, labelDao, accountLabelDao)

        val targetAccount = map.keys.first()
        val targetLabels = map[targetAccount]!!

        val targetLabelIds = targetLabels.map { it.id }.toSet()

        val results =
            accountDao.getAccounts(labels = targetLabelIds, matchAllLabels = false).first()

        results.forEach { (_, labels) ->
            val labelIds = labels.map { it.id }.toSet()
            val section = targetLabelIds.intersect(labelIds)

            assertTrue(section.isNotEmpty(), "Didn't match any labels")
        }
    }
}