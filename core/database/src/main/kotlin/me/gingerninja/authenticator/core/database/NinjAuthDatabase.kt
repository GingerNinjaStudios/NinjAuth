package me.gingerninja.authenticator.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.gingerninja.authenticator.core.database.dao.AccountDao
import me.gingerninja.authenticator.core.database.dao.AccountLabelDao
import me.gingerninja.authenticator.core.database.dao.LabelDao
import me.gingerninja.authenticator.core.database.model.AccountEntity
import me.gingerninja.authenticator.core.database.model.AccountLabelEntity
import me.gingerninja.authenticator.core.database.model.LabelEntity
import me.gingerninja.authenticator.core.database.util.AccountAlgorithmConverter
import me.gingerninja.authenticator.core.database.util.AccountSourceConverter
import me.gingerninja.authenticator.core.database.util.AccountTypeConverter
import me.gingerninja.authenticator.core.database.util.InstantConverter
import net.sqlcipher.database.SQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        AccountLabelEntity::class,
        LabelEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
@TypeConverters(
    AccountTypeConverter::class,
    AccountSourceConverter::class,
    AccountAlgorithmConverter::class,
    InstantConverter::class,
)
abstract class NinjAuthDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    abstract fun labelDao(): LabelDao

    abstract fun accountLabelDao(): AccountLabelDao

    fun changePassword(charArray: CharArray) {
        // as we created the database with the SQL Cipher factory, we can retrieve the encrypted DB
        val db = openHelper.writableDatabase as SQLiteDatabase
        db.changePassword(charArray)
    }
}

