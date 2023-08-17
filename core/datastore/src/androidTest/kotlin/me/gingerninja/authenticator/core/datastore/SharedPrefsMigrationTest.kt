package me.gingerninja.authenticator.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class SharedPrefsMigrationTest {
    private lateinit var appContext: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settings: NinjAuthSettings

    private val testScope = TestScope(UnconfinedTestDispatcher())

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun createOldSharedPrefs() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        sharedPreferences = appContext.getSharedPreferences("app_settings", 0)

        sharedPreferences.edit()
            .putString("settings_appearance_theme", "system")
            .putString("settings_security_lock_type", "pin")
            .putString("settings_security_lock_leave", "120")
            .putBoolean("settings_security_hide_recent", true)
            .putBoolean("settings_security_bio_enabled", true)
            .putString("key_bio", "1234")
            .putString("db_pass", "5678")
            .putBoolean("settings_first_run_complete", true)
            .putInt("sec_bio_ver", 1)
            .apply()

        val dataSource = createTestDataStore(
            context = appContext,
            location = tempFolder,
            scope = testScope
        )

        settings = NinjAuthSettings(dataSource)
    }

    @After
    fun clear() {
        appContext.deleteSharedPreferences("app_settings")
    }

    @Test
    fun createNinjAuthSettingsWithOldData() = testScope.runTest {
        val data = settings.data.first()

        assertEquals("system", data.appearance.theme.value)
        assertEquals("pin", data.security.lockType.value)
        assertEquals(2.minutes, data.security.lockLeave)
        assertTrue(data.security.hideRecent)
        assertTrue(data.security.isBiometricsEnabled)
        assertEquals("1234", settings.getBiometricKey())
        assertEquals("5678", settings.getEncryptedDatabasePass())
        assertTrue(data.firstRunComplete)
        assertEquals(1, data.security.biometricsVersion)
    }
}