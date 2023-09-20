package me.gingerninja.authenticator.core.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.auth.biometric.BiometricAuthenticator
import me.gingerninja.authenticator.core.auth.biometric.BiometricKeyHandler
import me.gingerninja.authenticator.core.database.LegacyKeyDatabase
import me.gingerninja.authenticator.core.database.NinjAuthDatabaseAuthenticator
import me.gingerninja.authenticator.core.database.test.createInMemoryTestDatabaseBuilder
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.datastore.test.createTestDataStore
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.assertFalse

@LargeTest
class BiometricAuthTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var context: Context

    private lateinit var settings: NinjAuthSettings

    private lateinit var authenticator: BiometricAuthenticator

    @get:Rule(order = 0)
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
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

        val biometricKeyHandler = BiometricKeyHandler(context, settings)

        authenticator = BiometricAuthenticator(
            context = context,
            dispatcher = testDispatcher,
            settings = settings,
            dbAuthenticator = dbAuthenticator,
            biometricKeyHandler = biometricKeyHandler
        )

        assumeTrue(authenticator.isAvailable)
    }

    @After
    @Throws(IOException::class)
    fun close() {
        LegacyKeyDatabase
            .getIfExists(context)
            ?.delete() // FIXME this should not be called, instead the using class should use an in-memory DB
    }

    @Test
    fun canAuthenticate_false() = testScope.runTest {
        assertFalse(authenticator.canAuthenticate())
    }
}