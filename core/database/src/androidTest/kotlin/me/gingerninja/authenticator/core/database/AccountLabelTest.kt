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

class AccountLabelTest {
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

    @Test
    fun getAccountsWithLabels() = runTest {
        val map = createLinkedEntries(accountDao, labelDao, accountLabelDao)

        val accountLabels = accountDao.getAccounts().first()
        assertEquals(map.size, accountLabels.size, "Not all accounts are in the DB")

        accountLabels.forEach { accountLabel ->
            val mappedLabels = map[accountLabel.account] ?: emptySet()

            assertEquals(
                mappedLabels.size,
                accountLabel.labels.size,
                "The account doesn't have all the labels"
            )

            mappedLabels.forEachIndexed { index, expectedLabel ->
                assertEquals(expectedLabel, accountLabel.labels[index], "Wrong label found")
            }
        }
    }

    @Test
    fun updateAccountLabelAssociation() = runTest {
        val map = createLinkedEntries(accountDao, labelDao, accountLabelDao)

        val accountLabels = accountDao.getAccounts().first()
        assertEquals(map.size, accountLabels.size, "Not all accounts are in the DB")

        // updating account labels of the 0th account
        accountLabelDao.update(accountLabels[0].account.id, emptySet())

        val accountLabelsUpdated = accountDao.getAccounts().first()
        assertEquals(map.size, accountLabelsUpdated.size, "Not all accounts are in the DB")

        accountLabelsUpdated.forEachIndexed { accIndex, accountLabel ->
            // artificially checking the 0th account to have empty labels
            val mappedLabels = if (accIndex == 0) {
                emptySet()
            } else {
                map[accountLabel.account] ?: emptySet()
            }

            assertEquals(
                mappedLabels.size,
                accountLabel.labels.size,
                "The account doesn't have all the labels"
            )

            mappedLabels.forEachIndexed { index, expectedLabel ->
                assertEquals(expectedLabel, accountLabel.labels[index], "Wrong label found")
            }
        }
    }
}