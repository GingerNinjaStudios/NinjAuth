package me.gingerninja.authenticator.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.gingerninja.authenticator.core.common.ApplicationScope
import me.gingerninja.authenticator.core.common.Dispatcher
import me.gingerninja.authenticator.core.common.DispatcherType.IO
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Singleton
    @Provides
    fun providePreferenceDataSource(
        @ApplicationContext context: Context,
        @Dispatcher(IO) dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            migrations = NinjAuthSettings.getSharedPrefsMigrations(context),
            scope = CoroutineScope(scope.coroutineContext + dispatcher)
        ) {
            context.preferencesDataStoreFile("app_settings")
        }
    }
}