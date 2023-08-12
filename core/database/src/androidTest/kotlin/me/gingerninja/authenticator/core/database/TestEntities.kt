package me.gingerninja.authenticator.core.database

import kotlinx.coroutines.flow.first
import me.gingerninja.authenticator.core.database.dao.AccountDao
import me.gingerninja.authenticator.core.database.dao.AccountLabelDao
import me.gingerninja.authenticator.core.database.dao.LabelDao
import me.gingerninja.authenticator.core.database.model.AccountEntity
import me.gingerninja.authenticator.core.database.model.LabelEntity
import kotlin.test.assertEquals

val testTotpAccounts = listOf(
    AccountEntity(
        id = 0,
        title = "Croissant",
        issuer = "Issuer #1",
        accountName = "Test account #1",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZR",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 0
    ),
    AccountEntity(
        id = 0,
        title = "Anthony",
        issuer = "Issuer #2",
        accountName = "Test account #2",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZS",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 1
    ),
    AccountEntity(
        id = 0,
        title = "Homeland",
        issuer = "Issuer #3",
        accountName = "Test account #3",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZT",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 2
    )
)

val testTotpAccountsWithId = listOf(
    AccountEntity(
        id = 1,
        uid = "acc1",
        title = "Croissant",
        issuer = "Issuer #1",
        accountName = "Test account #1",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZR",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 0
    ),
    AccountEntity(
        id = 2,
        uid = "acc2",
        title = "Anthony",
        issuer = "Issuer #2",
        accountName = "Test account #2",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZS",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 1
    ),
    AccountEntity(
        id = 3,
        uid = "acc3",
        title = "Homeland",
        issuer = "Issuer #3",
        accountName = "Test account #3",
        secret = "KRSXG5BAMFRWG33VNZ2CAIZT",
        typeSpecificData = 30,
        type = AccountEntity.Type.TOTP,
        digits = 6,
        position = 2
    )
)

val testLabels = listOf(
    LabelEntity(
        id = 0,
        name = "Red",
        color = 0xff0000,
        position = 0
    ),
    LabelEntity(
        id = 0,
        name = "Green",
        color = 0x00ff00,
        position = 1
    ),
    LabelEntity(
        id = 0,
        name = "Blue",
        color = 0x0000ff,
        position = 2
    )
)

val testLabelsWithId = listOf(
    LabelEntity(
        id = 1,
        uid = "label1",
        name = "Red",
        color = 0xff0000,
        position = 0
    ),
    LabelEntity(
        id = 2,
        uid = "label2",
        name = "Green",
        color = 0x00ff00,
        position = 1
    ),
    LabelEntity(
        id = 3,
        uid = "label3",
        name = "Blue",
        color = 0x0000ff,
        position = 2
    )
)

/**
 * Adds the accounts and labels to the database and creates a link between them.
 */
suspend fun createLinkedEntries(
    accountDao: AccountDao,
    labelDao: LabelDao,
    accountLabelDao: AccountLabelDao
): Map<AccountEntity, Set<LabelEntity>> {
    val addedAccounts = testTotpAccounts.mapNotNull {
        val id = accountDao.saveAccount(it)
        accountDao.getAccountWithLabels(id)?.account
    }

    val accountCount = accountDao.getAccounts().first().size
    assertEquals(testTotpAccounts.size, accountCount, "Didn't insert all accounts")

    val addedLabels = testLabels.mapNotNull {
        labelDao.run { saveLabel(it) }.let { id -> labelDao.getLabel(id) }
    }

    val labelCount = labelDao.getLabelsWithCounter().first().size
    assertEquals(testLabels.size, labelCount, "Didn't insert all labels")

    return buildMap {
        addedAccounts.forEachIndexed { index, account ->
            val labels = addedLabels.subList(index, minOf(index + 2, addedLabels.size)).toSet()
            put(account, labels)

            accountLabelDao.update(account.id, labels.map { it.id }.toSet())
        }
    }
}