package me.gingerninja.authenticator.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.gingerninja.authenticator.database.NinjAuthDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Singleton
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
    }
}