package me.gingerninja.authenticator.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "AccountHasLabel",
    primaryKeys = ["account", "label"],
    indices = [
        Index("account"),
        Index("label")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["id"],
            childColumns = ["label"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AccountLabelEntity(
    @ColumnInfo(name = "account")
    val accountId: Long,

    @ColumnInfo(name = "label")
    val labelId: Long,

    @ColumnInfo(name = "position", defaultValue = "0")
    val position: Int = 0,
)

data class AccountWithLabels(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = LabelEntity::class,
        associateBy = Junction(
            value = AccountLabelEntity::class,
            parentColumn = "account",
            entityColumn = "label"
        )
    )
    val labels: List<LabelEntity>
)