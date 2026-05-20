package me.gingerninja.authenticator.feature.settings.security.password

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import me.gingerninja.authenticator.core.design.component.NinjaButton
import me.gingerninja.authenticator.core.design.component.NinjaMessageCard
import me.gingerninja.authenticator.core.design.component.NinjaPasswordField
import me.gingerninja.authenticator.core.design.component.rememberSecretTextFieldState
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.navigateIfResumed
import me.gingerninja.authenticator.core.preview.ThemeWrapper
import me.gingerninja.authenticator.feature.settings.R
import me.gingerninja.authenticator.feature.settings.component.PreAuthController
import me.gingerninja.authenticator.feature.settings.getPreAuthController
import me.gingerninja.authenticator.core.ui.design.R as commonR

@Composable
internal fun SetPasswordScreen(
    lockType: SecurityConfig.LockType,
    modifier: Modifier = Modifier,
    preAuthController: PreAuthController = getPreAuthController(),
    viewModel: SetPasswordViewModel = hiltViewModel(
        creationCallback = { factory: SetPasswordViewModel.Factory ->
            factory.create(
                newLockType = lockType,
                preAuthController = preAuthController,
            )
        },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val goBack = navigateIfResumed {
        popBackStack(NinjaScreen.Settings.LockTypeChooser(state.oldLockType), true)
    }

    SideEffect(state.isUpdated) {
        if (state.isUpdated) {
            goBack()
        }
    }

    SetPasswordScreen(
        modifier = modifier,
        state = state,
        onPasswordSubmit = viewModel::setPassword,
        onDismissError = viewModel::dismissError,
    )
}

@Composable
private fun SetPasswordScreen(
    state: SetPasswordUiState,
    onPasswordSubmit: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusRequester = remember { FocusRequester() }
    val passwordState = rememberSecretTextFieldState()
    val password2State = rememberSecretTextFieldState()
    val enabled by rememberUpdatedState(state.enabled)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    val valid =
        passwordState.value.isNotEmpty() && password2State.value.isNotEmpty() && passwordState.value == password2State.value

    val isPin = state.lockType == SecurityConfig.LockType.PIN

    BackHandler(!enabled) {
        // do not allow back if not enabled
    }

    LaunchedEffect(lifecycleOwner, enabled) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (enabled) {
                focusRequester.requestFocus()
            }
        }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isPin) {
                            stringResource(R.string.settings_security_new_pin_label)
                        } else {
                            stringResource(R.string.settings_security_new_password_label)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateIfResumed {
                            popBackStack()
                        },
                        enabled = enabled,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NinjaMessageCard(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_security_set_password_message),
                icon = commonR.drawable.ic_info,
            )

            Spacer(modifier = Modifier.height(32.dp))

            NinjaPasswordField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                state = passwordState,
                enabled = enabled,
                isPin = isPin,
                focusRequester = focusRequester,
                onSubmit = null,
                imeAction = ImeAction.Next,
                label = if (isPin) {
                    stringResource(R.string.settings_security_new_pin_label)
                } else {
                    stringResource(R.string.settings_security_new_password_label)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            NinjaPasswordField(
                modifier = Modifier.fillMaxWidth(),
                state = password2State,
                enabled = enabled,
                error = if (password2State.value.isNotEmpty() && password2State.value != passwordState.value) {
                    if (isPin) {
                        stringResource(R.string.settings_security_set_password_no_match_passwords)
                    } else {
                        stringResource(R.string.settings_security_set_password_no_match_pins)
                    }
                } else {
                    null
                },
                isPin = isPin,
                focusRequester = focusRequester,
                onSubmit = null,
                imeAction = ImeAction.Done,
                label = if (isPin) {
                    stringResource(R.string.settings_security_confirm_pin_label)
                } else {
                    stringResource(R.string.settings_security_confirm_password_label)
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            NinjaButton(
                onClick = {
                    passwordState.showSecret = false
                    onPasswordSubmit(passwordState.value.toString())
                },
                enabled = enabled && valid,
            ) {
                Text(text = stringResource(id = R.string.auth_btn_continue))
            }
        }
    }
}

@Preview
@PreviewWrapper(ThemeWrapper::class)
@Composable
private fun SettingsAuthScreenPreview() {
    SetPasswordScreen(
        state = SetPasswordUiState(
            enabled = true,
            lockType = SecurityConfig.LockType.PASSWORD,
            oldLockType = SecurityConfig.LockType.PASSWORD,
        ),
        onPasswordSubmit = {},
        onDismissError = {},
    )
}