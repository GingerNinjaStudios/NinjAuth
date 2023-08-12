package me.gingerninja.authenticator.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.gingerninja.authenticator.core.database.NinjAuthDatabase
import me.gingerninja.authenticator.core.database.model.AccountEntity
import me.gingerninja.authenticator.core.database.model.AccountWithLabels
import java.util.concurrent.Callable
import kotlin.random.Random

@Dao
interface AccountDao {
    /*@Query("SELECT * FROM Account WHERE id = :id")
    suspend fun getAccount(id: Long): AccountEntity*/

    @Transaction
    @Query(
        value = """
        SELECT Account.* FROM Account 
        LEFT JOIN AccountHasLabel ON AccountHasLabel.account = Account.id 
        LEFT JOIN Label ON Label.id = AccountHasLabel.label
        WHERE Account.id = :id
        ORDER BY Account.position, AccountHasLabel.position
        """,
    )
    suspend fun getAccountWithLabels(id: Long): AccountWithLabels?

    @Query("SELECT id FROM Account WHERE uid = :uid")
    suspend fun getAccountIdByUid(uid: String): Long?

    @Transaction
    @Query(
        value = """
        SELECT Account.* FROM Account 
        LEFT JOIN AccountHasLabel ON AccountHasLabel.account = Account.id 
        LEFT JOIN Label ON Label.id = AccountHasLabel.label
        WHERE
            CASE WHEN :search IS NOT NULL AND :search <> ''
                THEN (
                    Account.accountName LIKE '%' || :search || '%' OR
                    Account.title LIKE '%' || :search || '%' OR
                    Account.issuer LIKE '%' || :search || '%'
                )
                ELSE 1
            END
            AND 
            CASE WHEN :labelCount > 0
                THEN AccountHasLabel.label IN (:labels)
                ELSE 1
            END
        GROUP BY Account.id
        HAVING
            CASE WHEN :matchAllLabels AND :labelCount > 0
                THEN COUNT(Account.id) = :labelCount -- the number of label IDs to match, if "matchAllLabels" is true
                ELSE 1
            END
        ORDER BY Account.position, AccountHasLabel.position
        """,
    )
    fun getAccounts(
        search: String? = null,
        labels: Collection<Long>? = null,
        labelCount: Int = labels?.size ?: 0,
        matchAllLabels: Boolean = false
    ): Flow<List<AccountWithLabels>>

    /**
     * Saves the account to the database.
     *
     * @param account the account to save
     * @return the new account ID on insertion or -1 if an update was performed
     */
    @Transaction
    suspend fun saveAccount(account: AccountEntity): Long {
        var retryCount = 0
        var entity = if (account.uid.isBlank()) account.withNewUid() else account

        do {
            if (entity.id != 0L || getAccountIdByUid(entity.uid) == null) {
                return saveAccountWithoutRetry(entity)
            } else {
                retryCount++
                entity = entity.withNewUid()
            }
        } while (retryCount < 10)

        throw RuntimeException("Cannot create unique UID") // TODO define proper exception
    }

    /**
     * Saves the entity without retry. This should not be used directly. Use [saveAccount] instead.
     *
     * @see saveAccount
     */
    @Upsert
    suspend fun saveAccountWithoutRetry(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM Account WHERE id = :id")
    suspend fun deleteAccount(id: Long)
}