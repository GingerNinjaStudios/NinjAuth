package me.gingerninja.authenticator.feature.settings.security.biometric

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.gingerninja.authenticator.core.design.component.MessageRow
import me.gingerninja.authenticator.core.design.component.NinjaButton
import me.gingerninja.authenticator.core.design.component.NinjaMessageCard
import me.gingerninja.authenticator.core.design.component.NinjaMessageCardDefaults
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.navigateIfResumed
import me.gingerninja.authenticator.feature.settings.R
import me.gingerninja.authenticator.feature.settings.component.PreAuthContainer
import me.gingerninja.authenticator.feature.settings.component.PreAuthController
import me.gingerninja.authenticator.feature.settings.getPreAuthController
import me.gingerninja.authenticator.core.ui.design.R as commonR

@Composable
internal fun SettingsBiometricScreen(
    modifier: Modifier = Modifier,
    preAuthController: PreAuthController = getPreAuthController(),
    viewModel: SettingsBiometricViewModel = hiltViewModel(
        creationCallback = { factory: SettingsBiometricViewModel.Factory ->
            factory.create(preAuthController)
        },
    ),
) {
    val activity = LocalActivity.current as FragmentActivity
    val state by viewModel.state.collectAsStateWithLifecycle()

    val goBack = navigateIfResumed {
        popBackStack()
    }

    SideEffect(state.isUpdated) {
        if (state.isUpdated) {
            goBack()
        }
    }

    SettingsBiometricScreen(
        modifier = modifier,
        preAuthController = preAuthController,
        state = state,
        onBiometricsToggle = {
            if (state.enabled) {
                if (state.isBiometricsEnabled) {
                    viewModel.disable()
                } else {
                    viewModel.enable(
                        activity = activity,
                        password = preAuthController.passwordFieldState.value,
                    )
                }
            }
        },
        onDismissError = viewModel::dismissError,
    )
}

@Composable
private fun SettingsBiometricScreen(
    preAuthController: PreAuthController,
    state: BiometricUiState,
    onBiometricsToggle: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preAuthState by preAuthController.state.collectAsStateWithLifecycle()
    val canGoBack = preAuthState.enabled || (preAuthState.isAuthenticated && state.enabled)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    BackHandler(!canGoBack) {
        // do not go back if not enabled
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_security_biometric_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateIfResumed {
                            popBackStack()
                        },
                        enabled = canGoBack,
                    ) {
                        Icon(
                            painter = painterResource(commonR.drawable.ic_back),
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    ) { padding ->
        PreAuthContainer(
            modifier = Modifier.padding(padding),
            controller = preAuthController,
            message = stringResource(R.string.settings_pre_auth_message),
        ) {
            BiometricContent(
                modifier = Modifier.fillMaxSize(),
                enabled = state.enabled,
                lockTypeMissing = state.lockType == SecurityConfig.LockType.NONE,
                biometricsEnabled = state.isBiometricsEnabled,
                onBiometricsToggle = onBiometricsToggle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BiometricContent(
    biometricsEnabled: Boolean,
    onBiometricsToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    lockTypeMissing: Boolean = false,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (lockTypeMissing) {
            NinjaMessageCard(
                modifier = Modifier.fillMaxWidth(),
                colors = NinjaMessageCardDefaults.errorColors(

                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MessageRow(
                        text = stringResource(R.string.settings_security_biometric_missing_app_lock_message),
                        icon = commonR.drawable.ic_warning,
                        contentPadding = PaddingValues.Zero,
                    )

                    NinjaButton(
                        onClick = navigateIfResumed {
                            navigate(NinjaScreen.Settings.LockTypeChooser(SecurityConfig.LockType.NONE))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.settings_security_biometric_setup_app_lock_button),
                        )
                    }
                }
            }
        }

        NinjaMessageCard(
            text = stringResource(R.string.settings_security_biometric_warning_message),
        )

        NinjaButton(
            enabled = enabled && !lockTypeMissing,
            onClick = onBiometricsToggle,
        ) {
            Text(
                text = stringResource(
                    if (biometricsEnabled) {
                        R.string.settings_security_biometric_disable
                    } else {
                        R.string.settings_security_biometric_enable
                    }
                ),
            )
        }
    }
}