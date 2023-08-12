package me.gingerninja.authenticator.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import me.gingerninja.authenticator.core.model.Label

@Entity(
    tableName = "Label",
    indices = [
        Index("uid", unique = true)
    ],
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Long,

    @ColumnInfo(name = "icon", defaultValue = "null")
    val icon: String? = null,

    @ColumnInfo(name = "position", defaultValue = "-1")
    val position: Int = -1
)

fun LabelEntity.asModel() = Label(
    id = id,
    uid = uid,
    name = name,
    color = color,
    icon = icon,
    position = position
)