package me.gingerninja.authenticator.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.gingerninja.authenticator.core.database.model.LabelEntity
import me.gingerninja.authenticator.core.model.Label

@Dao
interface LabelDao {
    @Query("SELECT * FROM Label WHERE id = :id")
    suspend fun getLabel(id: Long): LabelEntity?

    @Query("SELECT id FROM Label WHERE uid = :uid")
    suspend fun getLabelIdByUid(uid: String): Long?

    /*@Query(
        value = """
            SELECT * FROM Label
            ORDER BY Label.position
        """
    )
    fun getLabels(): List<LabelEntity>*/

    @Query(
        value = """
            SELECT Label.*, COUNT(AccountHasLabel.account) as accountCount FROM Label
            LEFT JOIN AccountHasLabel ON AccountHasLabel.label = Label.id
            GROUP BY Label.id
            ORDER BY Label.position
        """
    )
    fun getLabelsWithCounter(): Flow<List<LabelWithAccountCount>>

    /**
     * Saves the label to the database.
     *
     * @param label the label to save
     * @return the new label ID on insertion or -1 if an update was performed
     */
    @Transaction
    suspend fun saveLabel(label: LabelEntity): Long {
        var retryCount = 0
        var entity = if (label.uid.isBlank()) label.withNewUid() else label

        do {
            if (entity.id != 0L || getLabelIdByUid(entity.uid) == null) {
                return saveLabelWithoutRetry(entity)
            } else {
                retryCount++
                entity = entity.withNewUid()
            }
        } while (retryCount < 10)

        throw RuntimeException("Cannot create unique UID") // TODO define proper exception
    }

    /**
     * Saves the label without retry. This should not be used directly. Use [saveLabel] instead.
     *
     * @param label the label to save
     * @return the new label ID on insertion or -1 if an update was performed
     */
    @Upsert
    suspend fun saveLabelWithoutRetry(label: LabelEntity): Long

    @Delete
    suspend fun deleteLabel(label: LabelEntity)

    @Query("DELETE FROM Label WHERE id = :id")
    suspend fun deleteLabel(id: Long)
}

data class LabelWithAccountCount(
    @Embedded val label: LabelEntity,

    @ColumnInfo("accountCount")
    val count: Int
    /*@Relation(
        parentColumn = "id",
        entityColumn = "label",
        entity = AccountLabelEntity::class,
        projection = []
    )
    val labels: List<LabelEntity>*/
)

fun LabelWithAccountCount.asModel() = Label(
    id = label.id,
    uid = label.uid,
    name = label.name,
    color = label.color,
    icon = label.icon,
    position = label.position,
    numberOfAccounts = count
)