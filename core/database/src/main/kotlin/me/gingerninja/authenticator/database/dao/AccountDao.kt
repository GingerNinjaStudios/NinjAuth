package me.gingerninja.authenticator.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.gingerninja.authenticator.database.model.AccountEntity
import me.gingerninja.authenticator.database.model.AccountWithLabels

@Dao
interface AccountDao {
    /*@Query("SELECT * FROM Account WHERE id = :id")
    suspend fun getAccount(id: Long): AccountEntity*/

    @Transaction
    @Query(
        value = """
        SELECT * FROM Account 
        LEFT JOIN AccountHasLabel ON AccountHasLabel.account = Account.id 
        LEFT JOIN Label ON Label.id = AccountHasLabel.label
        WHERE Account.id = :id
        ORDER BY Account.position, AccountHasLabel.position
        """,
    )
    suspend fun getAccountWithLabels(id: Long): AccountWithLabels

    @Transaction
    @Query(
        value = """
        SELECT * FROM Account 
        LEFT JOIN AccountHasLabel ON AccountHasLabel.account = Account.id 
        LEFT JOIN Label ON Label.id = AccountHasLabel.label
        GROUP BY Account.id
        ORDER BY Account.position, AccountHasLabel.position
        """,
    )
    fun getAccounts(): Flow<List<AccountWithLabels>>

    /**
     * Saves the account to the database.
     *
     * @param account the label to save
     * @return the new account ID on insertion or -1 if an update was performed
     */
    @Upsert
    suspend fun saveAccount(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM Account WHERE id = :id")
    suspend fun deleteAccount(id: Long)
}