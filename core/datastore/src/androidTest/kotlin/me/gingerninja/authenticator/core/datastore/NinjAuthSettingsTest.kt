package me.gingerninja.authenticator.core.datastore

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.datastore.test.createTestDataStore
import me.gingerninja.authenticator.core.model.settings.AppearanceConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class NinjAuthSettingsTest {
    private lateinit var settings: NinjAuthSettings

    private val testScope = TestScope(UnconfinedTestDispatcher())

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun createNinjAuthSettings() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val dataSource = createTestDataStore(
            context = appContext,
            location = tempFolder,
            scope = testScope
        )

        settings = NinjAuthSettings(dataSource)
    }

    @Test
    fun defaultNinjAuthSettings() = testScope.runTest {
        val data = settings.data.first()

        assertEquals(NinjAuthSettings.Defaults, data)
    }

    @Test
    fun biometricsToggle() = testScope.runTest {
        settings.setBiometricKey("EjRWeJA=")

        val bioEnabledData = settings.data.first()

        assertTrue(bioEnabledData.security.isBiometricsEnabled)
        assertEquals("EjRWeJA=", settings.getBiometricKey())

        settings.disableBiometrics()

        val bioDisabledData = settings.data.first()

        assertFalse(bioDisabledData.security.isBiometricsEnabled)
        assertNull(settings.getBiometricKey())
    }

    @Test
    fun changeTheme() = testScope.runTest {
        settings.setTheme(AppearanceConfig.Theme.LIGHT)
        settings.data.first().also {
            assertEquals(AppearanceConfig.Theme.LIGHT, it.appearance.theme)
        }

        settings.setTheme(AppearanceConfig.Theme.DARK)
        settings.data.first().also {
            assertEquals(AppearanceConfig.Theme.DARK, it.appearance.theme)
        }
    }

    @Test
    fun completeFirstRun() = testScope.runTest {
        settings.data.first().also {
            assertFalse(it.firstRunComplete)
        }

        settings.setFirstRunComplete()

        settings.data.first().also {
            assertTrue(it.firstRunComplete)
        }
    }

    @Test
    fun encryptedDbPass() = testScope.runTest {
        settings.setEncryptedDatabasePass("EjRWeJA=")
        settings.getEncryptedDatabasePass().also {
            assertEquals("EjRWeJA=", it)
        }

        settings.setEncryptedDatabasePass(null)
        settings.getEncryptedDatabasePass().also {
            assertNull(it)
        }
    }

    @Test
    fun securityLockLeave() = testScope.runTest {
        settings.setSecurityLockLeave(SecurityConfig.NeverLock)
        settings.data.first().also {
            assertEquals(SecurityConfig.NeverLock, it.security.lockLeave)
            assertFalse(it.security.shouldLockWhenLeave)
        }

        settings.setSecurityLockLeave(1.minutes)
        settings.data.first().also {
            assertEquals(1.minutes, it.security.lockLeave)
            assertTrue(it.security.shouldLockWhenLeave)
        }
    }

    @Test
    fun securityHideFromRecents() = testScope.runTest {
        settings.setSecurityHideFromRecents(true)
        settings.data.first().also {
            assertTrue(it.security.hideRecent)
        }

        settings.setSecurityHideFromRecents(false)
        settings.data.first().also {
            assertFalse(it.security.hideRecent)
        }
    }

    @Test
    fun securityLockType() = testScope.runTest {
        settings.setSecurityLockType(SecurityConfig.LockType.NONE)
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.NONE, it.security.lockType)
        }

        settings.setSecurityLockType(SecurityConfig.LockType.PIN)
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.PIN, it.security.lockType)
        }

        settings.setSecurityLockType(SecurityConfig.LockType.PASSWORD)
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.PASSWORD, it.security.lockType)
        }
    }

    @Test
    fun securitySetLockTypeWithPass() = testScope.runTest {
        settings.setSecurityWithDatabasePass(SecurityConfig.LockType.NONE, null)
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.NONE, it.security.lockType)
        }
        assertNull(settings.getEncryptedDatabasePass())

        settings.setSecurityWithDatabasePass(SecurityConfig.LockType.PIN, "EjRWeJA=")
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.PIN, it.security.lockType)
        }
        assertEquals("EjRWeJA=", settings.getEncryptedDatabasePass())

        settings.setSecurityWithDatabasePass(SecurityConfig.LockType.PASSWORD, "AjRWeJA=")
        settings.data.first().also {
            assertEquals(SecurityConfig.LockType.PASSWORD, it.security.lockType)
        }
        assertEquals("AjRWeJA=", settings.getEncryptedDatabasePass())
    }
}