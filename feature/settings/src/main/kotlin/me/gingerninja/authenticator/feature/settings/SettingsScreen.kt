package me.gingerninja.authenticator.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.gingerninja.authenticator.core.model.settings.AppearanceConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.model.settings.SecurityConfig.Companion.ImmediateLock
import me.gingerninja.authenticator.core.model.settings.SecurityConfig.Companion.NeverLock
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.navigateIfResumed
import me.gingerninja.authenticator.feature.settings.component.LicensesDialog
import me.gingerninja.authenticator.feature.settings.component.SettingsCategory
import me.gingerninja.authenticator.feature.settings.component.SettingsItem
import me.gingerninja.authenticator.feature.settings.component.SettingsOption
import me.gingerninja.authenticator.feature.settings.utils.getLockoutTitle
import me.gingerninja.authenticator.feature.settings.utils.titleRes
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        state = state,
        onThemeChange = viewModel::setTheme,
        onDynamicColorsChange = viewModel::setDynamicColors,
        onLockTimeoutChange = viewModel::setLockTimeout,
        onHideFromRecentsChange = viewModel::setHideFromRecents,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onThemeChange: (AppearanceConfig.Theme) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onLockTimeoutChange: (Duration) -> Unit,
    onHideFromRecentsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listScrollState = rememberScrollState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        scrollState = listScrollState,
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateIfResumed {
                            popBackStack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_back),
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(listScrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppearanceSettings(
                appearanceConfig = state.appearanceConfig,
                onThemeChange = onThemeChange,
                onDynamicColorsChange = onDynamicColorsChange,
            )

            SecuritySettings(
                config = state.securityConfig,
                onLockTimeoutChange = onLockTimeoutChange,
                onHideFromRecentsChange = onHideFromRecentsChange,
            )

            AppInfoSettings(
                appVersion = state.appVersion,
            )
        }
    }
}

@Composable
private fun AppearanceSettings(
    appearanceConfig: AppearanceConfig,
    onThemeChange: (AppearanceConfig.Theme) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCategory(
        modifier = modifier,
        title = stringResource(R.string.settings_appearance_title),
    ) {
        SettingsItem(
            title = stringResource(R.string.settings_appearance_theme_title),
            value = appearanceThemeList.firstOrNull { it.value == appearanceConfig.theme },
            onValueChange = {
                onThemeChange(it.value)
            },
            options = appearanceThemeList,
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_theme),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsItem(
                title = stringResource(R.string.settings_appearance_dynamic_colors_title),
                value = stringResource(R.string.settings_appearance_dynamic_colors_description),
                checked = appearanceConfig.dynamicColors,
                onCheckedChange = {
                    onDynamicColorsChange(it)
                },
                iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_wallpaper),
            )
        }
    }
}

@Composable
private fun SecuritySettings(
    config: SecurityConfig,
    onLockTimeoutChange: (Duration) -> Unit,
    onHideFromRecentsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lockTimeoutList = securityLockTimeoutList

    val currentLockType by rememberUpdatedState(config.lockType)

    val openBiometric = navigateIfResumed {
        navigate(
            NinjaScreen.Settings.Biometric(
                lockType = currentLockType,
            )
        )
    }

    SettingsCategory(
        modifier = modifier,
        title = stringResource(R.string.settings_security_title),
    ) {
        SettingsItem(
            title = stringResource(R.string.settings_security_lock_type_title),
            value = stringResource(config.lockType.titleRes),
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_lock),
            onClick = navigateIfResumed {
                navigate(
                    NinjaScreen.Settings.LockTypeChooser(
                        lockType = currentLockType,
                    )
                )
            },
        )

        SettingsItem(
            title = stringResource(R.string.settings_security_biometric_title),
            value = stringResource(R.string.settings_security_biometric_description),
            checked = config.isBiometricsEnabled,
            onCheckedChange = {
                openBiometric()
            },
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_fingerprint),
        )

        SettingsItem(
            title = stringResource(R.string.settings_security_lock_timeout_title),
            value = lockTimeoutList.firstOrNull { it.value == config.lockLeave },
            enabled = config.lockType != SecurityConfig.LockType.NONE,
            options = lockTimeoutList,
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_timer),
            onValueChange = {
                onLockTimeoutChange(it.value)
            },
        )

        SettingsItem(
            title = stringResource(R.string.settings_security_hide_recent_title),
            value = stringResource(R.string.settings_security_hide_recent_description),
            checked = config.hideRecent,
            onCheckedChange = onHideFromRecentsChange,
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_visibility_off),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppInfoSettings(
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    SettingsCategory(
        modifier = modifier,
        title = stringResource(R.string.settings_app_info_title),
    ) {
        SettingsItem(
            title = stringResource(R.string.settings_app_info_version_title),
            value = appVersion,
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_info),
            onClick = {
                // TODO
            },
        )

        SettingsItem(
            title = stringResource(R.string.settings_app_info_licenses_title),
            value = stringResource(R.string.settings_app_info_licenses_description),
            iconPainter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_description),
            onClick = {
                showLicenses = true
            },
        )
    }

    LicensesDialog(
        show = showLicenses,
        onDismiss = {
            showLicenses = false
        },
    )
}

private val appearanceThemeList = buildList {
    add(
        SettingsOption(
            titleRes = R.string.settings_appearance_theme_type_dark,
            value = AppearanceConfig.Theme.DARK,
        )
    )

    add(
        SettingsOption(
            titleRes = R.string.settings_appearance_theme_type_light,
            value = AppearanceConfig.Theme.LIGHT,
        )
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        add(
            SettingsOption(
                titleRes = R.string.settings_appearance_theme_type_system,
                value = AppearanceConfig.Theme.SYSTEM,
            ),
        )
    }

    add(
        SettingsOption(
            titleRes = R.string.settings_appearance_theme_type_battery_saver,
            value = AppearanceConfig.Theme.BATTERY_SAVER,
        )
    )
}


private val securityLockTimeoutList: List<SettingsOption<Duration>>
    @Composable get() = buildList {
        listOf(
            ImmediateLock,
            5.seconds,
            15.seconds,
            30.seconds,
            1.minutes,
            2.minutes,
            5.minutes,
            10.minutes,
            30.minutes,
            NeverLock,
        ).forEach {
            add(
                SettingsOption(
                    title = it.getLockoutTitle(),
                    value = it,
                )
            )
        }
    }