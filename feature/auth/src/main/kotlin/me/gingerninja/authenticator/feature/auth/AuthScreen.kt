package me.gingerninja.authenticator.feature.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import me.gingerninja.authenticator.core.design.component.NinjaSecretTextField
import me.gingerninja.authenticator.core.design.component.NinjaSecretTextFieldState
import me.gingerninja.authenticator.core.design.component.rememberSecretTextFieldState
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.ui.DevicePreviews
import me.gingerninja.authenticator.core.ui.design.R as commonR

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthComplete: () -> Unit,
) {
    val activity = LocalActivity.current as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    val runBiometric = remember(viewModel, activity) {
        fun() {
            viewModel.authenticate(activity)
        }
    }

    LaunchedEffect(viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.state.collectLatest {
                if (it?.isAuthenticated == true) {
                    onAuthComplete()
                }
            }
        }
    }

    LaunchedEffect(state?.enabled, lifecycleOwner, viewModel, runBiometric, focusRequester) {
        if (state?.enabled == true) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.authMode.collectLatest {
                    when (it) {
                        AuthMode.NONE -> {
                            // don't care
                        }

                        AuthMode.PASSWORD -> {
                            focusRequester.requestFocus()
                        }

                        AuthMode.BIOMETRIC -> runBiometric()
                    }
                }
            }
        }
    }

    state?.let {
        AuthScreen(
            modifier = modifier,
            isReauthenticating = it.isReauthenticating,
            passwordState = viewModel.passwordState,
            enabled = it.enabled,
            lockType = it.lockType,
            error = it.authError,
            isBiometricAvailable = it.isBiometricAvailable,
            onPasswordSubmit = viewModel::authenticate,
            onBiometricClick = runBiometric,
            onConfirmExit = {
                activity.finishAffinity()
            },
            focusRequester = focusRequester
        )
    }
}

@Composable
private fun AuthScreen(
    modifier: Modifier,
    isReauthenticating: Boolean,
    passwordState: NinjaSecretTextFieldState,
    enabled: Boolean,
    lockType: SecurityConfig.LockType,
    isBiometricAvailable: Boolean,
    error: AuthError? = null,
    onPasswordSubmit: () -> Unit,
    onBiometricClick: () -> Unit,
    onConfirmExit: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }
    // TODO
) {
    if (isReauthenticating) {
        ConfirmExitDialog(
            onConfirm = onConfirmExit
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isReauthenticating) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                text = stringResource(id = R.string.auth_intermediate_message),
                style = MaterialTheme.typography.titleMedium
            )
        }

        PasswordField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            state = passwordState,
            enabled = enabled,
            isError = error?.wrongPass ?: false,
            lockType = lockType,
            focusRequester = focusRequester,
            onSubmit = onPasswordSubmit
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    passwordState.showSecret = false
                    onPasswordSubmit(/*passwordFieldState.value*/)
                },
                enabled = enabled && passwordState.value.isNotEmpty(),
            ) {
                Text(text = stringResource(id = R.string.btn_login))
            }

            if (isBiometricAvailable) {
                ButtonSeparator()

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBiometricClick,
                    enabled = enabled,
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp),
                        painter = painterResource(commonR.drawable.ic_fingerprint),
                        contentDescription = null
                    )
                    Text(text = stringResource(id = R.string.auth_use_biometric), softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun ConfirmExitDialog(
    onConfirm: () -> Unit
) {
    val (showDialog, setShowDialog) = rememberSaveable { mutableStateOf(false) }

    BackHandler {
        setShowDialog(true)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = stringResource(id = R.string.auth_confirm_exit_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowDialog(false) }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.auth_confirm_exit_dialog_title))
            },
            text = {
                Text(text = stringResource(id = R.string.auth_confirm_exit_dialog_message))
            }
        )
    }
}

@Composable
private fun PasswordField(
    modifier: Modifier = Modifier,
    state: NinjaSecretTextFieldState,
    enabled: Boolean,
    isError: Boolean,
    lockType: SecurityConfig.LockType,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester,
) {
    val keyboardOptions = remember(lockType) {
        val keyboardType = when (lockType) {
            SecurityConfig.LockType.NONE -> KeyboardType.Text
            SecurityConfig.LockType.PIN -> KeyboardType.NumberPassword
            SecurityConfig.LockType.PASSWORD -> KeyboardType.Password
        }

        KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done)
    }

    val (showError, setShowError) = rememberSaveable(isError) {
        mutableStateOf(isError)
    }

    DisposableEffect(state.value) {
        setShowError(false)
        onDispose { }
    }

    val errorTextRes = remember(showError, lockType) {
        if (showError) {
            when (lockType) {
                SecurityConfig.LockType.PIN -> R.string.auth_error_wrong_pin
                SecurityConfig.LockType.PASSWORD -> R.string.auth_error_wrong_password
                else -> null
            }
        } else {
            null
        }
    }

    NinjaSecretTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        isError = showError,
        textAlign = if (lockType == SecurityConfig.LockType.PIN) TextAlign.Center else TextAlign.Start,
        supportingText = errorTextRes?.let { stringResource(id = it) }
            ?: "", // intentionally using empty string as placeholder
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions {
            if (state.value.isNotEmpty()) {
                state.showSecret = false
                focusRequester.freeFocus()
                onSubmit()
            }
        }
    )
}

@Composable
private fun ButtonSeparator(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = stringResource(id = R.string.separator_or),
            style = MaterialTheme.typography.labelSmall,
            color = DividerDefaults.color
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
    }
}

@DevicePreviews
@PreviewLightDark
@Composable
private fun AuthScreenPreview() {
    AuthScreen(
        modifier = Modifier.fillMaxSize(),
        isReauthenticating = false,
        passwordState = rememberSecretTextFieldState(),
        enabled = true,
        lockType = SecurityConfig.LockType.PIN,
        isBiometricAvailable = true,
        error = null,
        onPasswordSubmit = {},
        onBiometricClick = {},
        onConfirmExit = {},
    )
}