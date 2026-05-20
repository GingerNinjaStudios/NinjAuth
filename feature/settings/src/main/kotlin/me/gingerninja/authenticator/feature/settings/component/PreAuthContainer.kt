package me.gingerninja.authenticator.feature.settings.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.auth.password.PasswordAuthException
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator.AuthConfig
import me.gingerninja.authenticator.core.common.toDatabaseByteArray
import me.gingerninja.authenticator.core.design.anim.DefaultMotionAnimOffset
import me.gingerninja.authenticator.core.design.anim.motionEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionExitTransition
import me.gingerninja.authenticator.core.design.component.NinjaButton
import me.gingerninja.authenticator.core.design.component.NinjaMessageCard
import me.gingerninja.authenticator.core.design.component.NinjaPasswordField
import me.gingerninja.authenticator.core.design.component.NinjaSecretTextFieldState
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.core.preview.PreviewContainerWrapper
import me.gingerninja.authenticator.feature.settings.R
import me.gingerninja.authenticator.core.ui.design.R as commonR

@Composable
internal fun PreAuthContainer(
    controller: PreAuthController,
    modifier: Modifier = Modifier,
    message: String? = stringResource(R.string.settings_pre_auth_message),
    content: @Composable () -> Unit,
) {
    val animOffset = DefaultMotionAnimOffset

    val state by controller.state.collectAsStateWithLifecycle()

    AnimatedContent(
        modifier = modifier,
        targetState = state.isAuthenticated,
        transitionSpec = {
            motionEnterTransition(animOffset) togetherWith motionExitTransition(animOffset)
        },
    ) { authenticated ->
        if (authenticated) {
            content()
        } else {
            val controllerState by controller.state.collectAsStateWithLifecycle()

            AuthForm(
                state = controllerState,
                passwordState = controller.passwordFieldState,
                onAuthenticate = controller::authenticate,
                message = message,
            )
        }
    }
}

@Composable
private fun AuthForm(
    state: PreAuthController.PreAuthUiState,
    passwordState: NinjaSecretTextFieldState,
    onAuthenticate: () -> Unit,
    message: String? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusRequester = remember { FocusRequester() }
    val enabled by rememberUpdatedState(state.enabled)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (message != null) {
            NinjaMessageCard(
                modifier = Modifier.fillMaxWidth(),
                text = message,
                icon = commonR.drawable.ic_lock,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        NinjaPasswordField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            state = passwordState,
            enabled = enabled,
            isError = state.authError?.wrongPass ?: false,
            isPin = state.lockType == SecurityConfig.LockType.PIN,
            focusRequester = focusRequester,
            onSubmit = onAuthenticate,
        )

        Spacer(modifier = Modifier.height(8.dp))

        NinjaButton(
            onClick = {
                passwordState.showSecret = false
                onAuthenticate()
            },
            enabled = enabled && passwordState.value.isNotEmpty(),
        ) {
            Text(text = stringResource(id = R.string.auth_btn_continue))
        }
    }
}

@Stable
internal class PreAuthController(
    private val scope: CoroutineScope,
    private val passwordAuthenticator: PasswordAuthenticator,
    initialLockType: SecurityConfig.LockType,
) {
    internal val passwordFieldState = NinjaSecretTextFieldState(TextFieldState())

    val state: StateFlow<PreAuthUiState>
        field = MutableStateFlow(
            PreAuthUiState(
                lockType = initialLockType,
                isAuthenticated = initialLockType == SecurityConfig.LockType.NONE,
                enabled = initialLockType != SecurityConfig.LockType.NONE,
            )
        )

    val lockType get() = state.value.lockType

    internal val password: String get() = passwordFieldState.value.toString()

    internal suspend fun update(
        lockType: SecurityConfig.LockType,
        newPassword: String?,
    ) {
        check(lockType == SecurityConfig.LockType.NONE || newPassword != null)

        val oldLockType = this.lockType

        when (lockType) {
            SecurityConfig.LockType.NONE -> {
                passwordAuthenticator.disable(
                    config = PasswordAuthenticator.DisableConfig(
                        password = password.toDatabaseByteArray(),
                    )
                )
            }

            SecurityConfig.LockType.PIN,
            SecurityConfig.LockType.PASSWORD -> {
                if (oldLockType == SecurityConfig.LockType.NONE) {
                    enablePassword(
                        lockType = lockType,
                        newPassword = newPassword!!,
                    )
                } else {
                    updatePassword(
                        lockType = lockType,
                        newPassword = newPassword,
                    )
                }
            }
        }

        passwordFieldState.textFieldState.setTextAndPlaceCursorAtEnd(newPassword ?: "")
        state.update {
            it.copy(
                lockType = lockType,
            )
        }
    }

    private suspend fun enablePassword(
        lockType: SecurityConfig.LockType,
        newPassword: String,
    ) {
        passwordAuthenticator.enable(
            config = PasswordAuthenticator.EnableConfig(
                password = newPassword.toDatabaseByteArray(),
                type = lockType,
            )
        )
    }

    private suspend fun updatePassword(
        lockType: SecurityConfig.LockType,
        newPassword: String?,
    ) {
        passwordAuthenticator.update(
            config = PasswordAuthenticator.UpdateConfig(
                oldPassword = password.toDatabaseByteArray(),
                newPassword = newPassword!!.toDatabaseByteArray(),
                type = lockType,
            )
        )
    }

    internal fun authenticate() {
        scope.launch {
            state.update {
                it.copy(
                    enabled = false,
                    authError = null,
                )
            }

            try {
                passwordAuthenticator.authenticate(
                    AuthConfig(passwordFieldState.value.toDatabaseByteArray())
                )

                state.update {
                    it.copy(
                        authError = null,
                        isAuthenticated = true,
                    )
                }
            } catch (_: PasswordAuthException) {
                state.update {
                    it.copy(
                        enabled = true,
                        authError = PreAuthError(wrongPass = true),
                    )
                }
            }
        }
    }

    data class PreAuthUiState(
        val lockType: SecurityConfig.LockType,
        val enabled: Boolean = true,
        val authError: PreAuthError? = null,
        val isAuthenticated: Boolean = false,
    )

    data class PreAuthError(
        val wrongPass: Boolean,
    )
}

@Preview
@PreviewWrapper(PreviewContainerWrapper::class)
@Composable
private fun AuthFormPreview() {
    AuthForm(
        state = PreAuthController.PreAuthUiState(
            isAuthenticated = false,
            enabled = true,
            lockType = SecurityConfig.LockType.PIN,
        ),
        passwordState = NinjaSecretTextFieldState(TextFieldState()),
        onAuthenticate = {},
        message = stringResource(R.string.settings_pre_auth_message)
    )
}