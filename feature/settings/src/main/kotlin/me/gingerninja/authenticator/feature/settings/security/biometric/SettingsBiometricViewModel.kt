package me.gingerninja.authenticator.feature.settings.security.biometric

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.auth.biometric.BiometricAuthenticator
import me.gingerninja.authenticator.core.auth.biometric.BiometricException
import me.gingerninja.authenticator.core.common.toDatabaseByteArray
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.feature.settings.R
import me.gingerninja.authenticator.feature.settings.component.PreAuthController

@HiltViewModel(assistedFactory = SettingsBiometricViewModel.Factory::class)
internal class SettingsBiometricViewModel @AssistedInject constructor(
    @Assisted private val preAuthController: PreAuthController,
    @param:ApplicationContext private val context: Context,
    private val settings: NinjAuthSettings,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {
    private val internalState = MutableStateFlow(
        BiometricUiState(
            lockType = preAuthController.lockType,
            enabled = true,
        )
    )

    internal val state: StateFlow<BiometricUiState> = combine(internalState, settings.data)
    { internalState, settings ->
        BiometricUiState(
            enabled = internalState.enabled,
            isUpdated = internalState.isUpdated,
            isBiometricsEnabled = settings.security.isBiometricsEnabled,
            lockType = settings.security.lockType,
            error = internalState.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = internalState.value
    )

    fun enable(
        activity: FragmentActivity,
        password: CharSequence,
    ) {
        internalState.update {
            it.copy(
                enabled = false,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                biometricAuthenticator.enable(
                    config = BiometricAuthenticator.EnableConfig(
                        activity = activity,
                        password = password.toDatabaseByteArray(),
                        descriptor = BiometricAuthenticator.PromptDescriptor(
                            title = context.getString(R.string.settings_security_biometric_enable),
                            negativeButton = context.getString(android.R.string.cancel),
                        )
                    )
                )

                internalState.update {
                    it.copy(
                        isUpdated = true,
                    )
                }
            } catch (e: BiometricException) {
                val errorMessage = when (e.code) {
                    BiometricException.Error.USER_CANCELED,
                    BiometricException.Error.NEGATIVE_BUTTON -> null

                    else -> e.message
                }

                internalState.update {
                    it.copy(
                        enabled = true,
                        error = errorMessage,
                    )
                }
            } catch (e: Throwable) {
                internalState.update {
                    it.copy(
                        enabled = true,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun disable() {
        internalState.update {
            it.copy(
                enabled = false,
            )
        }

        viewModelScope.launch {
            biometricAuthenticator.disable()
            internalState.update {
                it.copy(
                    isUpdated = true,
                )
            }
        }
    }

    fun dismissError() {
        internalState.update {
            it.copy(
                error = null,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(preAuthController: PreAuthController): SettingsBiometricViewModel
    }
}

internal data class BiometricUiState(
    val enabled: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
    val isUpdated: Boolean = false,
    val lockType: SecurityConfig.LockType,
    val error: String? = null,
)