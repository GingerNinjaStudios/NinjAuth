package me.gingerninja.authenticator.core.database.model

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import me.gingerninja.authenticator.core.model.Label
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.random.Random

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

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Long,

    @ColumnInfo(name = "icon", defaultValue = "null")
    val icon: String? = null,

    @ColumnInfo(name = "position", defaultValue = "-1")
    val position: Int = -1,

    @ColumnInfo(name = "uid")
    val uid: String = generateLabelUID(name = name, icon = icon, color = color),
) {
    fun withNewUid() = copy(uid = generateUID(Random.nextBytes(8)))

    private fun generateUID(
        random: ByteArray? = null,
        name: String = this.name,
        icon: String? = this.icon,
        color: Long = this.color
    ) = generateLabelUID(
        random = random,
        name = name,
        icon = icon,
        color = color
    )
}

fun LabelEntity.asModel() = Label(
    id = id,
    uid = uid,
    name = name,
    color = color,
    icon = icon,
    position = position
)

private fun generateLabelUID(
    random: ByteArray? = null,
    name: String,
    icon: String?,
    color: Long
): String {
    val digest = MessageDigest.getInstance("SHA-384").apply {
        update(name.toByteArray(StandardCharsets.UTF_8))

        if (icon != null) {
            update(icon.toByteArray(StandardCharsets.UTF_8))
        }

        val buffer = ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE)
        buffer.putInt(color.toInt())

        update(buffer)

        if (random != null) {
            update(random)
        }
    }

    return Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
}