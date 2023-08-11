package me.gingerninja.authenticator.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.gingerninja.authenticator.database.NinjAuthDatabase
import me.gingerninja.authenticator.database.dao.AccountDao
import me.gingerninja.authenticator.database.dao.AccountLabelDao
import me.gingerninja.authenticator.database.dao.LabelDao

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun providesAccountDao(
        database: NinjAuthDatabase,
    ): AccountDao = database.accountDao()

    @Provides
    fun providesLabelDao(
        database: NinjAuthDatabase,
    ): LabelDao = database.labelDao()

    @Provides
    fun providesAccountLabelDao(
        database: NinjAuthDatabase,
    ): AccountLabelDao = database.accountLabelDao()
}