package me.gingerninja.authenticator.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import me.gingerninja.authenticator.database.model.AccountLabelEntity

@Dao
interface AccountLabelDao {

    @Query(
        value = """
        DELETE FROM AccountHasLabel
        WHERE account = :accountId AND label NOT IN (:labelIds)
        """
    )
    fun deleteNotInLabels(accountId: Long, labelIds: Set<Long>)

    @Upsert
    fun saveAccountLabels(accountLabels: List<AccountLabelEntity>)

    @Transaction
    fun update(accountId: Long, labelIds: Set<Long>) {
        deleteNotInLabels(accountId, labelIds)

        val entities = labelIds.mapIndexed { index, labelId ->
            AccountLabelEntity(accountId, labelId, index)
        }

        saveAccountLabels(entities)
    }
}