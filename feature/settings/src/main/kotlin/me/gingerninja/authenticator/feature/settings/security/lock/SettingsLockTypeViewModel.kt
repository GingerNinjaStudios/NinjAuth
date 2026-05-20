package me.gingerninja.authenticator.feature.settings.security.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.feature.settings.component.PreAuthController

@HiltViewModel(assistedFactory = SettingsLockTypeViewModel.Factory::class)
internal class SettingsLockTypeViewModel @AssistedInject constructor(
    @Assisted private val preAuthController: PreAuthController,
    private val settings: NinjAuthSettings,
) : ViewModel() {
    private val _lockTypeNavigation: Channel<LockTypeNavigation> = Channel()
    internal val lockTypeNavigation = _lockTypeNavigation.receiveAsFlow()

    private val internalState = MutableStateFlow(
        LockTypeUiState(
            enabled = true,
            lockType = preAuthController.lockType,
        )
    )

    internal val state: StateFlow<LockTypeUiState> =
        combine(internalState, settings.data) { internalState, settings ->
            LockTypeUiState(
                enabled = internalState.enabled,
                lockType = settings.security.lockType,
                showRemoveWarningDialog = internalState.showRemoveWarningDialog,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = LockTypeUiState(
                lockType = preAuthController.lockType,
            )
        )

    fun onLockTypeSelected(type: SecurityConfig.LockType) {
        val currentLockType = state.value.lockType

        when (type) {
            SecurityConfig.LockType.NONE if currentLockType == type -> {
                _lockTypeNavigation.trySend(LockTypeNavigation.Back)
            }

            SecurityConfig.LockType.NONE -> {
                internalState.update {
                    it.copy(
                        showRemoveWarningDialog = true,
                    )
                }
            }

            else -> {
                _lockTypeNavigation.trySend(LockTypeNavigation.SetPassword(type))
            }
        }
    }

    fun confirmLockRemove() {
        internalState.update {
            it.copy(
                enabled = false,
                showRemoveWarningDialog = false,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                preAuthController.update(
                    lockType = SecurityConfig.LockType.NONE,
                    newPassword = null,
                )

                _lockTypeNavigation.trySend(LockTypeNavigation.Back)
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

    fun dismissLockRemove() {
        internalState.update {
            it.copy(
                showRemoveWarningDialog = false,
            )
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
        fun create(preAuthController: PreAuthController): SettingsLockTypeViewModel
    }
}

internal data class LockTypeUiState(
    val enabled: Boolean = true,
    val lockType: SecurityConfig.LockType,
    val showRemoveWarningDialog: Boolean = false,
    val error: String? = null,
)

internal sealed interface LockTypeNavigation {
    data object Back : LockTypeNavigation
    data class SetPassword(val type: SecurityConfig.LockType) : LockTypeNavigation
}