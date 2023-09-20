package me.gingerninja.authenticator.core.database.test

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.gingerninja.authenticator.core.database.NinjAuthDatabase
import me.gingerninja.authenticator.core.database.di.DbModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DbModule::class],
)
object TestDataStoreModule {
    /*@Provides
    @Singleton
    fun providesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,

        /**
         * Temporary folder for the app settings file. This needs to be provided by the tests using
         * `@BindValue`.
         */
        tmpFolder: TemporaryFolder,
    ): DataStore<Preferences> =
        createTestDataStore(
            context = context,
            location = tmpFolder,
            scope = scope,
        )*/
}

fun createInMemoryTestDatabaseBuilder(
    context: Context
): RoomDatabase.Builder<NinjAuthDatabase> {
    return Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java)
}

fun createTestDatabaseBuilder(
    context: Context
): RoomDatabase.Builder<NinjAuthDatabase> {
    return Room.databaseBuilder(context, NinjAuthDatabase::class.java, TEST_DATABASE_NAME)
}

const val TEST_DATABASE_NAME = "test.db"