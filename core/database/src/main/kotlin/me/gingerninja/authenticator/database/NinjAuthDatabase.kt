package me.gingerninja.authenticator.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.gingerninja.authenticator.database.dao.AccountDao
import me.gingerninja.authenticator.database.dao.AccountLabelDao
import me.gingerninja.authenticator.database.dao.LabelDao
import me.gingerninja.authenticator.database.model.AccountEntity
import me.gingerninja.authenticator.database.model.AccountLabelEntity
import me.gingerninja.authenticator.database.model.LabelEntity
import me.gingerninja.authenticator.database.util.AccountAlgorithmConverter
import me.gingerninja.authenticator.database.util.AccountSourceConverter
import me.gingerninja.authenticator.database.util.AccountTypeConverter
import me.gingerninja.authenticator.database.util.InstantConverter

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
}

