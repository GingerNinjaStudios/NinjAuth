package me.gingerninja.authenticator.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.gingerninja.authenticator.core.model.settings.AppearanceConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.model.settings.UserSettings
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class NinjAuthSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val data = dataStore.data.map {
        UserSettings(
            appearance = AppearanceConfig(
                theme = AppearanceConfig.Theme.fromString(it[Keys.appearanceTheme])
                    ?: Defaults.appearance.theme
            ),
            security = SecurityConfig(
                lockType = SecurityConfig.LockType.fromString(it[Keys.securityLockType])
                    ?: Defaults.security.lockType,
                lockLeave = it[Keys.securityLockLeave]?.seconds
                    ?: Defaults.security.lockLeave,
                isBiometricsEnabled = it[Keys.securityBioEnabled]
                    ?: Defaults.security.isBiometricsEnabled,
                hideRecent = it[Keys.securityHideRecent]
                    ?: Defaults.security.hideRecent,
                biometricsVersion = it[Keys.securityBioVersion]
                    ?: Defaults.security.biometricsVersion,
            ),
            firstRunComplete = it[Keys.firstRunComplete] ?: Defaults.firstRunComplete
        )
    }

    suspend fun getBiometricKey() = dataStore.data.first()[Keys.authBiometricKey]

    suspend fun setBiometricKey(data: String?) {
        dataStore.edit {
            if (data == null) {
                it -= Keys.authBiometricKey
                it -= Keys.securityBioVersion
                it += Keys.securityBioEnabled to false
            } else {
                it += Keys.authBiometricKey to data
                it += Keys.securityBioEnabled to true
            }
        }
    }

    suspend fun getEncryptedDatabasePass() = dataStore.data.first()[Keys.authDbPass]

    suspend fun setSecurityWithDatabasePass(lockType: SecurityConfig.LockType, encrypted: String?) {
        if (lockType != SecurityConfig.LockType.NONE) {
            requireNotNull(encrypted)
        }

        dataStore.edit {
            it[Keys.securityLockType] = lockType.value
            if (encrypted == null) {
                it -= Keys.authDbPass
            } else {
                it[Keys.authDbPass] = encrypted
            }
        }
    }

    suspend fun setEncryptedDatabasePass(data: String?) {
        dataStore.edit {
            if (data == null) {
                it -= Keys.authDbPass // TODO set to something default instead of removing it?
            } else {
                it += Keys.authDbPass to data
            }
        }
    }

    /**
     * Alias for [setBiometricKey] with `null` as param.
     */
    suspend fun disableBiometrics() {
        setBiometricKey(null)
        setBiometricVersion(null)
    }

    suspend fun setFirstRunComplete() {
        saveValue(Keys.firstRunComplete, true)
    }

    suspend fun setTheme(theme: AppearanceConfig.Theme) {
        saveValue(Keys.appearanceTheme, theme.value)
    }

    suspend fun setSecurityLockType(lockType: SecurityConfig.LockType) {
        saveValue(Keys.securityLockType, lockType.value)
    }

    suspend fun setSecurityLockLeave(value: Duration) {
        saveValue(Keys.securityLockLeave, value.inWholeSeconds.toInt())
    }

    suspend fun setSecurityHideFromRecents(value: Boolean) {
        saveValue(Keys.securityHideRecent, value)
    }

    suspend fun setBiometricVersion(version: Int?) {
        saveValue(Keys.securityBioVersion, version)
    }

    private suspend fun <T> saveValue(key: Preferences.Key<T>, value: T?) {
        dataStore.edit {
            if (value == null) {
                it.remove(key)
            } else {
                it[key] = value
            }
        }
    }

    internal object Keys {
        val authBiometricKey = stringPreferencesKey("key_bio")
        val authDbPass = stringPreferencesKey("db_pass")
        val securityLockType = stringPreferencesKey("settings_security_lock_type")
        val securityBioEnabled = booleanPreferencesKey("settings_security_bio_enabled")
        val securityLockLeave = intPreferencesKey("settings_security_lock_leave")
        val securityHideRecent = booleanPreferencesKey("settings_security_hide_recent")
        val firstRunComplete = booleanPreferencesKey("settings_first_run_complete")
        val appearanceTheme = stringPreferencesKey("settings_appearance_theme")
        val securityBioVersion = intPreferencesKey("sec_bio_ver")
    }

    companion object {
        val Defaults = UserSettings(
            appearance = AppearanceConfig(
                theme = AppearanceConfig.Theme.DARK
            ),
            security = SecurityConfig(
                lockType = SecurityConfig.LockType.NONE,
                lockLeave = 0.seconds,
                isBiometricsEnabled = false,
                hideRecent = true,
                biometricsVersion = null,
            ),
            firstRunComplete = false
        )

        fun getSharedPrefsMigrations(
            context: Context,
            name: String = "app_settings"
        ): List<DataMigration<Preferences>> =
            listOf(
                SecurityLockLeaveMigration(
                    context = context,
                    name = name
                ),
                SharedPreferencesMigration(
                    context = context,
                    sharedPreferencesName = name,
                    keysToMigrate = setOf(
                        Keys.authBiometricKey.name,
                        Keys.authDbPass.name,
                        Keys.securityLockType.name,
                        Keys.securityBioEnabled.name,
                        // intentionally not included: Keys.securityLockLeave.name,
                        Keys.securityHideRecent.name,
                        Keys.firstRunComplete.name,
                        Keys.appearanceTheme.name,
                        Keys.securityBioVersion.name,
                    )
                ),
            )
    }
}

/**
 * Special migration class because the old app stored the "settings_security_lock_leave" value
 * as string instead of int and it causes the regular [SharedPreferencesMigration] to fail.
 */
private class SecurityLockLeaveMigration(
    private val context: Context,
    private val name: String,
) : DataMigration<Preferences> {
    private var sharedPrefs: SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private val keyName = NinjAuthSettings.Keys.securityLockLeave.name

    override suspend fun cleanUp() {
        sharedPrefs
            .edit()
            .remove(keyName)
            .apply {
                if (!commit()) {
                    throw IOException("Unable to delete $keyName")
                }
            }

        if (sharedPrefs.all.isEmpty()) {
            context.deleteSharedPreferences(name)
        }
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return sharedPrefs.contains(keyName)
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutablePreferences = currentData.toMutablePreferences()

        if (sharedPrefs.contains(keyName)) {
            mutablePreferences[NinjAuthSettings.Keys.securityLockLeave] = sharedPrefs.getString(
                keyName,
                null
            )?.toIntOrNull() ?: NinjAuthSettings.Defaults.security.lockLeave.inWholeSeconds.toInt()
        }

        return mutablePreferences.toPreferences()
    }
}