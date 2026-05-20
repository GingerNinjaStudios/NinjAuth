package me.gingerninja.authenticator.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.AppearanceConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: NinjAuthSettings,
) : ViewModel() {

    internal val state: StateFlow<SettingsUiState> = settings.data.map { settings ->
        SettingsUiState(
            loading = false,
            appearanceConfig = settings.appearance,
            securityConfig = settings.security,
            appVersion = getAppVersion(),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SettingsUiState(
                appVersion = getAppVersion(),
            )
        )

    internal fun setTheme(theme: AppearanceConfig.Theme) {
        viewModelScope.launch {
            settings.setTheme(theme)
        }
    }

    internal fun setDynamicColors(dynamicColors: Boolean) {
        viewModelScope.launch {
            settings.setDynamicColors(dynamicColors)
        }
    }

    internal fun setLockTimeout(timeout: Duration) {
        viewModelScope.launch {
            settings.setSecurityLockLeave(timeout)
        }
    }

    internal fun setHideFromRecents(hide: Boolean) {
        viewModelScope.launch {
            settings.setSecurityHideFromRecents(hide)
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }
}

internal data class SettingsUiState(
    val loading: Boolean = true,
    val appearanceConfig: AppearanceConfig = NinjAuthSettings.Defaults.appearance,
    val securityConfig: SecurityConfig = NinjAuthSettings.Defaults.security,
    val appVersion: String = "",
)