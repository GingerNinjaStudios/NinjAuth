package me.gingerninja.authenticator.core.datastore.test

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import me.gingerninja.authenticator.core.common.ApplicationScope
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.datastore.di.DataStoreModule
import org.junit.rules.TemporaryFolder
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class],
)
object TestDataStoreModule {
    @Provides
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
        )
}

fun createTestDataStore(
    context: Context,
    location: TemporaryFolder,
    scope: CoroutineScope,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        migrations = NinjAuthSettings.getSharedPrefsMigrations(context),
        scope = scope
    ) {
        location.newFile("app_settings_test.preferences_pb")
    }
}