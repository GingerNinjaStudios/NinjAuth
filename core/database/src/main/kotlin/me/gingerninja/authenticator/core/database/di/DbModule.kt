package me.gingerninja.authenticator.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.gingerninja.authenticator.core.database.NinjAuthDatabase
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    fun providesDatabaseBuilder(@ApplicationContext context: Context): RoomDatabase.Builder<NinjAuthDatabase> {
        return Room
            .databaseBuilder(
                context,
                NinjAuthDatabase::class.java,
                "ninjauth.db"
            )
    }
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