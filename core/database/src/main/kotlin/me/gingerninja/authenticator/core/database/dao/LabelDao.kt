package me.gingerninja.authenticator.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.gingerninja.authenticator.core.database.model.LabelEntity

@Dao
interface LabelDao {
    @Query("SELECT * FROM Label WHERE id = :id")
    suspend fun getLabel(id: Long): LabelEntity

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
            GROUP BY AccountHasLabel.label
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
    @Upsert
    suspend fun saveLabel(label: LabelEntity): Long

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