package me.gingerninja.authenticator.core.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.auth.biometric.BiometricKeyHandler
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator
import me.gingerninja.authenticator.core.database.InvalidKeyPasswordException
import me.gingerninja.authenticator.core.database.LegacyKeyDatabase
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.database.test.createInMemoryTestDatabaseBuilder
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.datastore.test.createTestDataStore
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.assertEquals

//@HiltAndroidTest
class PasswordAuthTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var context: Context

    private lateinit var settings: NinjAuthSettings

    private lateinit var authenticator: PasswordAuthenticator

    @get:Rule(order = 0)
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()

        val dataSource = createTestDataStore(
            context = context,
            location = tempFolder,
            scope = testScope
        )

        settings = NinjAuthSettings(dataSource)

        val dbAuthenticator = NinjAuthDatabaseAuthenticator {
            createInMemoryTestDatabaseBuilder(context)
        }

        authenticator = PasswordAuthenticator(
            context = context,
            dispatcher = testDispatcher,
            settings = settings,
            dbAuthenticator = dbAuthenticator,
            biometricKeyHandler = BiometricKeyHandler(context, settings)
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        LegacyKeyDatabase
            .getIfExists(context)
            ?.delete() // FIXME this should not be called, instead the using class should use an in-memory DB
    }

    private suspend fun preparePassAuth() {
        val config = PasswordAuthenticator.EnableConfig(
            password = "testpass".toCharArray(),
            type = SecurityConfig.LockType.PASSWORD
        )

        authenticator.enable(config)
    }

    private suspend fun preparePINAuth() {
        val config = PasswordAuthenticator.EnableConfig(
            password = "12345678".toCharArray(),
            type = SecurityConfig.LockType.PIN
        )

        authenticator.enable(config)
    }

    @Test(expected = IllegalArgumentException::class)
    fun enable_invalidConfig() = testScope.runTest {
        val config = PasswordAuthenticator.EnableConfig(
            password = "testpass".toCharArray(),
            type = SecurityConfig.LockType.NONE
        )

        authenticator.enable(config)
    }

    @Test
    fun enable_authWithPassword() = testScope.runTest {
        preparePassAuth()

        assertEquals(SecurityConfig.LockType.PASSWORD, settings.data.first().security.lockType)
    }

    @Test
    fun authenticateWithPassword_valid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.AuthConfig(
            password = "testpass".toCharArray()
        )
        authenticator.authenticate(config)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun authenticateWithPassword_invalid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.AuthConfig(
            password = "wrongpass".toCharArray()
        )
        authenticator.authenticate(config)
    }

    @Test
    fun update_withPassword_valid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.UpdateConfig(
            oldPassword = "testpass".toCharArray(),
            newPassword = "newpass".toCharArray()
        )
        authenticator.update(config)

        // test the new password
        val config2 = PasswordAuthenticator.AuthConfig(
            password = "newpass".toCharArray()
        )
        authenticator.authenticate(config2)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun update_withPassword_invalid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.UpdateConfig(
            oldPassword = "wrongpass".toCharArray(),
            newPassword = "newpass".toCharArray()
        )
        authenticator.update(config)

        // test the new password
        val config2 = PasswordAuthenticator.AuthConfig(
            password = "newpass".toCharArray()
        )
        authenticator.authenticate(config2)
    }

    @Test
    fun disable_withPassword_valid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.DisableConfig(
            password = "testpass".toCharArray()
        )

        authenticator.disable(config)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun disable_withPassword_invalid() = testScope.runTest {
        preparePassAuth()

        val config = PasswordAuthenticator.DisableConfig(
            password = "wrongpass".toCharArray()
        )

        authenticator.disable(config)
    }

    @Test
    fun enable_authWithPIN() = testScope.runTest {
        preparePINAuth()

        assertEquals(SecurityConfig.LockType.PIN, settings.data.first().security.lockType)
    }

    @Test
    fun authenticateWithPIN_valid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.AuthConfig(
            password = "12345678".toCharArray()
        )
        authenticator.authenticate(config)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun authenticateWithPIN_invalid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.AuthConfig(
            password = "12211221".toCharArray()
        )
        authenticator.authenticate(config)
    }

    @Test
    fun update_withPIN_valid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.UpdateConfig(
            oldPassword = "12345678".toCharArray(),
            newPassword = "87654321".toCharArray()
        )
        authenticator.update(config)

        // test the new password
        val config2 = PasswordAuthenticator.AuthConfig(
            password = "87654321".toCharArray()
        )
        authenticator.authenticate(config2)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun update_withPIN_invalid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.UpdateConfig(
            oldPassword = "12211221".toCharArray(),
            newPassword = "87654321".toCharArray()
        )
        authenticator.update(config)

        // test the new password
        val config2 = PasswordAuthenticator.AuthConfig(
            password = "87654321".toCharArray()
        )
        authenticator.authenticate(config2)
    }

    @Test
    fun disable_withPIN_valid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.DisableConfig(
            password = "12345678".toCharArray()
        )

        authenticator.disable(config)
    }

    @Test(expected = InvalidKeyPasswordException::class)
    fun disable_withPIN_invalid() = testScope.runTest {
        preparePINAuth()

        val config = PasswordAuthenticator.DisableConfig(
            password = "12211221".toCharArray()
        )

        authenticator.disable(config)
    }
}