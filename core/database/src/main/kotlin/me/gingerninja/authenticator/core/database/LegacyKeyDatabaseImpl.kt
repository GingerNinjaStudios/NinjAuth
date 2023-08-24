package me.gingerninja.authenticator.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

internal class LegacyKeyDatabaseImpl(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), LegacyKeyDatabase {
    init {
        SQLiteDatabase.loadLibs(context)
    }

    private val db: SQLiteDatabase
        get() = getWritableDatabase(null as ByteArray?)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // do not care
    }

    override fun open(password: CharArray) {
        // this opens the database and caches its state inside
        try {
            getWritableDatabase(password)
        } catch (e: SQLiteException) {
            throw InvalidKeyPasswordException()
        }
    }

    override fun changePassword(password: CharArray) {
        db.changePassword(password)
    }

    override fun delete() {
        close()
        context.deleteDatabase(DB_NAME)
    }

    override fun get(name: String): LegacyKeyEntry {
        // the DB must be open already
        val keyBlob = db.query(
            LegacyKeyEntry.TABLE_NAME,
            arrayOf(LegacyKeyEntry.COL_VALUE),
            LegacyKeyEntry.COL_NAME + "=?",
            arrayOf(name),
            null,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getBlob(0)
            } else {
                throw IllegalArgumentException("The key $name does not exist")
            }
        }

        return LegacyKeyEntry(name, keyBlob)
    }

    override fun set(name: String, value: ByteArray) {
        val values = ContentValues(1)
        values.put(
            LegacyKeyEntry.COL_NAME,
            name
        )
        values.put(
            LegacyKeyEntry.COL_VALUE,
            value
        )

        // the DB must be open already
        db.insertWithOnConflict(
            LegacyKeyEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        ) != -1L
    }

    override fun set(entry: LegacyKeyEntry) {
        set(entry.name, entry.value)
    }

    override fun remove(name: String): Boolean {
        // the DB must be open already
        return db.delete(
            LegacyKeyEntry.TABLE_NAME,
            LegacyKeyEntry.COL_NAME + "=?",
            arrayOf(name)
        ) != 0
    }

    companion object {
        private const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS `${LegacyKeyEntry.TABLE_NAME}` (
                `${LegacyKeyEntry.COL_NAME}` TEXT NOT NULL,
                `${LegacyKeyEntry.COL_VALUE}` BLOB NOT NULL,
                PRIMARY KEY(`${LegacyKeyEntry.COL_NAME}`)
            )
        """
    }
}

interface LegacyKeyDatabase {
    operator fun get(name: String): LegacyKeyEntry

    fun set(entry: LegacyKeyEntry)

    operator fun set(name: String, value: ByteArray)

    fun remove(name: String): Boolean

    fun remove(entry: LegacyKeyEntry) {
        remove(entry.name)
    }

    fun open(password: CharArray)

    fun changePassword(password: CharArray)

    fun close()

    fun delete()

    companion object {
        const val MASTER_KEY_NAME = "dbMaster"

        fun exists(context: Context): Boolean = context.databaseList().contains(DB_NAME)

        fun getIfExists(context: Context): LegacyKeyDatabase? = if (exists(context)) {
            LegacyKeyDatabaseImpl(context)
        } else {
            null
        }
    }
}

data class LegacyKeyEntry(
    val name: String,
    val value: ByteArray,
) {
    companion object {
        internal const val TABLE_NAME = "key"
        internal const val COL_NAME = "name"
        internal const val COL_VALUE = "value"
    }

    fun invalidate() {
        value.fill(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LegacyKeyEntry

        if (name != other.name) return false
        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}

class InvalidKeyPasswordException : RuntimeException()

private const val DB_NAME = "_protomat.db"
private const val DB_VERSION = 1