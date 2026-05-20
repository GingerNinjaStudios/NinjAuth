package me.gingerninja.authenticator.feature.auth

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.text.input.TextFieldState
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.auth.biometric.BiometricAuthenticator
import me.gingerninja.authenticator.core.auth.biometric.BiometricException
import me.gingerninja.authenticator.core.auth.password.PasswordAuthException
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator
import me.gingerninja.authenticator.core.auth.unlocked.UnlockedAuthenticator
import me.gingerninja.authenticator.core.common.toDatabaseByteArray
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.design.component.NinjaSecretTextFieldState
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@SuppressLint("StaticFieldLeak")
@HiltViewModel(assistedFactory = AuthViewModel.Factory::class)
class AuthViewModel @AssistedInject constructor(
    @Assisted private val isReauthenticating: Boolean,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val settings: NinjAuthSettings,
    private val unlockedAuthenticator: UnlockedAuthenticator, // TODO remove
    private val passwordAuthenticator: PasswordAuthenticator,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {
    //private val args: AuthArgs = AuthArgs(savedStateHandle)

    /**
     * Holds the password state.
     *
     * Note that this is initialized and held here instead of in the composable as we want to keep
     * the entered value when the app config changes (e.g. screen orientation changes) without
     * possibly saving the value to the disk (which would happen with rememberSaveable).
     */
    internal val passwordState = NinjaSecretTextFieldState(TextFieldState())

    private val internalState = MutableStateFlow(
        AuthUiState(
            isReauthenticating = isReauthenticating,
            enabled = true,
            isBiometricAvailable = false,
            lockType = SecurityConfig.LockType.NONE
        )
    )

    internal val state = combine(
        settings.data,
        internalState
    ) { settingsData, uiData ->
        uiData.copy(
            enabled = uiData.enabled,
            isBiometricAvailable = biometricAuthenticator.canAuthenticate(),
            lockType = settingsData.security.lockType
        )
    }
        .onEach {
            if (!it.isAuthenticated && it.lockType == SecurityConfig.LockType.NONE) {
                runAuthentication { authenticate() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    private val internalAuthMode = Channel<AuthMode>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Indicates whether the [internalAuthMode] has been set to the proper initial [AuthMode]
     * based on the [NinjAuthSettings] values.
     */
    private var authModeInitialized: Boolean = false

    internal val authMode = internalAuthMode
        .receiveAsFlow()
        .onStart {
            if (!authModeInitialized) {
                authModeInitialized = true

                val validState = state.filterNotNull().first()

                if (validState.isBiometricAvailable) {
                    internalAuthMode.send(AuthMode.BIOMETRIC)
                } else if (validState.lockType != SecurityConfig.LockType.NONE) {
                    internalAuthMode.send(AuthMode.PASSWORD)
                } else {
                    internalAuthMode.send(AuthMode.NONE)
                }
            }
        }

    /**
     * Starts authentication for the user with the supplied password.
     */
    fun authenticate(/*password: String*/) {
        // TODO remove
        if (state.value?.lockType == SecurityConfig.LockType.NONE) {
            runAuthentication {
                unlockedAuthenticator.authenticate()
            }
            return
        }
        // --- end of remove ---

        val config = PasswordAuthenticator.AuthConfig(
            password = passwordState.value.toDatabaseByteArray()
        )

        runAuthentication {
            //passwordAuthenticator.authenticate(PasswordAuthenticator.AuthConfig("".toCharArray())) // TODO remove
            passwordAuthenticator.authenticate(config)
        }
    }

    /**
     * Starts authentication for the user with biometrics.
     */
    fun authenticate(activity: FragmentActivity) {
        viewModelScope.launch {
            val negativeBtnText =
                if (settings.data.firstOrNull()?.security?.lockType == SecurityConfig.LockType.PIN) {
                    R.string.auth_use_pin
                } else {
                    R.string.auth_use_password
                }

            val config = BiometricAuthenticator.AuthConfig(
                activity = activity,
                descriptor = BiometricAuthenticator.PromptDescriptor(
                    title = context.getString(R.string.auth_bio_title),
                    negativeButton = context.getString(negativeBtnText)
                )
            )

            runAuthentication {
                biometricAuthenticator.authenticate(config)
            }
        }
    }

    private fun runAuthentication(block: suspend () -> Unit) {
        internalState.update { it.copy(enabled = false, authError = null) }
        viewModelScope.launch {
            runCatching {
                block()
            }.onSuccess {
                handleSuccess()
            }.onFailure { e ->
                internalState.update { it.copy(enabled = true) }
                handleError(e)
            }
        }
    }

    private fun handleSuccess() {
        internalState.update {
            it.copy(authError = null, isAuthenticated = true)
        }
    }

    private fun handleError(e: Throwable) {
        Toast.makeText(context, e.message ?: "ERROR", Toast.LENGTH_SHORT).show()

        Log.e("LoginViewModel", "Auth error", e)
        when (e) {
            is BiometricException -> handleBiometricError(e)

            is PasswordAuthException -> handlePasswordError(e)

            else -> {
                // TODO generic error
            }
        }
    }

    private fun handlePasswordError(e: PasswordAuthException) {
        // TODO
        Log.e("LoginViewModel", "Password error: ${e.reason}")
        internalState.update {
            it.copy(authError = AuthError(wrongPass = true))
        }

        if (e.reason == PasswordAuthException.Reason.WRONG_PASSWORD) {
            internalAuthMode.trySend(AuthMode.PASSWORD)
        }
    }

    private fun handleBiometricError(e: BiometricException) {
        Log.e("LoginViewModel", "Biometric code: ${e.code}")
        when (e.code) {
            BiometricException.Error.SEC_KEY_INVALIDATED -> { /* TODO */
            }

            BiometricException.Error.SEC_SHOULD_RETRY -> { /* TODO */
            }

            BiometricException.Error.SEC_KEY_SECURITY_UPDATE -> { /* TODO */
            }

            BiometricException.Error.HW_UNAVAILABLE -> { /* TODO */
            }

            BiometricException.Error.UNABLE_TO_PROCESS -> { /* TODO */
            }

            BiometricException.Error.TIMEOUT -> { /* TODO */
            }

            BiometricException.Error.NO_SPACE -> { /* TODO */
            }

            BiometricException.Error.CANCELED -> { /* TODO */
            }

            BiometricException.Error.LOCKOUT -> { /* TODO */
            }

            BiometricException.Error.VENDOR -> { /* TODO */
            }

            BiometricException.Error.LOCKOUT_PERMANENT -> { /* TODO */
            }

            BiometricException.Error.USER_CANCELED -> { /* TODO */
            }

            BiometricException.Error.NO_BIOMETRICS -> { /* TODO */
            }

            BiometricException.Error.HW_NOT_PRESENT -> { /* TODO */
            }

            BiometricException.Error.NEGATIVE_BUTTON -> {
                internalAuthMode.trySend(AuthMode.PASSWORD)
            }

            BiometricException.Error.NO_DEVICE_CREDENTIAL -> { /* TODO */
            }

            BiometricException.Error.UNKNOWN -> { /* TODO */
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(isReauthenticating: Boolean): AuthViewModel
    }
}

internal data class AuthUiState(
    val isReauthenticating: Boolean,
    val enabled: Boolean,
    val isBiometricAvailable: Boolean,
    val lockType: SecurityConfig.LockType,
    val authError: AuthError? = null,
    val isAuthenticated: Boolean = false,
)

internal data class AuthError(
    val wrongPass: Boolean
)

internal enum class AuthMode {
    NONE, PASSWORD, BIOMETRIC
}

@OptIn(ExperimentalContracts::class)
fun Throwable.isBiometric(): Boolean {
    contract {
        returns(true) implies (this@isBiometric is BiometricException)
    }
    return this is BiometricException
}