package me.gingerninja.authenticator.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.gingerninja.authenticator.core.database.NinjAuthDatabase
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    /*@Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): NinjAuthDatabase {
        val defaultPass = "fakepass".toCharArray()
        val passphrase = SQLiteDatabase.getBytes(defaultPass)
        val factory = SupportFactory(passphrase)

        return Room
            .databaseBuilder(
                context,
                NinjAuthDatabase::class.java,
                "ninjauth"
            )
            .openHelperFactory(factory)
            .build()
    }*/

    /*@Singleton
    @Provides
    fun provideDatabase(handler: NinjAuthDatabaseAuthenticator): NinjAuthDatabase {
        return handler.database.value!!
    }*/
}