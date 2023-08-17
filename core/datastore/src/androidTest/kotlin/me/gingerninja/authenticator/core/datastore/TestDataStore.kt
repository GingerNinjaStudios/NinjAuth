package me.gingerninja.authenticator.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TemporaryFolder

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