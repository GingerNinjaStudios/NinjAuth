package me.gingerninja.authenticator.feature.settings.security.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.currentNavigator
import me.gingerninja.authenticator.core.navigation.navigateIfResumed
import me.gingerninja.authenticator.feature.settings.R
import me.gingerninja.authenticator.feature.settings.component.PreAuthContainer
import me.gingerninja.authenticator.feature.settings.component.PreAuthController
import me.gingerninja.authenticator.feature.settings.getPreAuthController
import me.gingerninja.authenticator.feature.settings.utils.titleRes
import me.gingerninja.authenticator.core.ui.design.R as commonR

@Composable
internal fun SettingsLockTypeScreen(
    modifier: Modifier = Modifier,
    preAuthController: PreAuthController = getPreAuthController(),
    viewModel: SettingsLockTypeViewModel = hiltViewModel(
        creationCallback = { factory: SettingsLockTypeViewModel.Factory ->
            factory.create(preAuthController)
        },
    ),
) {
    val navigator = currentNavigator
    val preAuthController = getPreAuthController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.lockTypeNavigation.collect { navigation ->
            when (navigation) {
                LockTypeNavigation.Back -> navigator.popBackStack()
                is LockTypeNavigation.SetPassword -> navigator.navigate(
                    NinjaScreen.Settings.PasswordSet(
                        lockType = navigation.type,
                    )
                )
            }
        }
    }

    SettingsLockTypeScreen(
        modifier = modifier,
        preAuthController = preAuthController,
        state = state,
        onSelected = { type -> viewModel.onLockTypeSelected(type) },
        onConfirmRemove = viewModel::confirmLockRemove,
        onDismissRemove = viewModel::dismissLockRemove,
        onDismissError = viewModel::dismissError,
    )
}

@Composable
private fun SettingsLockTypeScreen(
    preAuthController: PreAuthController,
    state: LockTypeUiState,
    onSelected: (SecurityConfig.LockType) -> Unit,
    onConfirmRemove: () -> Unit,
    onDismissRemove: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preAuthState by preAuthController.state.collectAsStateWithLifecycle()
    val enabled by rememberUpdatedState(state.enabled)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    BackHandler(!enabled) {
        // do not allow back if not enabled
    }

    if (state.showRemoveWarningDialog) {
        AlertDialog(
            onDismissRequest = onDismissRemove,
            confirmButton = {
                TextButton(onClick = onConfirmRemove) {
                    Text(text = stringResource(id = R.string.settings_security_lock_type_none_warning_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRemove) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.settings_security_lock_type_none_warning_title))
            },
            text = {
                Text(text = stringResource(id = R.string.settings_security_lock_type_none_warning_message))
            }
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_security_lock_type_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateIfResumed {
                            popBackStack()
                        },
                        enabled = (preAuthState.enabled || preAuthState.isAuthenticated) && enabled,
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
            LockTypeChooser(
                enabled = enabled,
                selected = state.lockType,
                onSelected = onSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LockTypeChooser(
    enabled: Boolean,
    selected: SecurityConfig.LockType,
    onSelected: (SecurityConfig.LockType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SecurityConfig.LockType.entries.forEach { lockType ->
            ListItem(
                enabled = enabled,
                selected = lockType == selected,
                onClick = {
                    onSelected(lockType)
                },
            ) {
                Text(
                    text = stringResource(lockType.titleRes),
                )
            }
        }
    }
}