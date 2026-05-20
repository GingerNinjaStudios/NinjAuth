package me.gingerninja.authenticator.feature.settings.security.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.feature.settings.component.PreAuthController

@HiltViewModel(assistedFactory = SetPasswordViewModel.Factory::class)
internal class SetPasswordViewModel @AssistedInject constructor(
    @Assisted private val newLockType: SecurityConfig.LockType,
    @Assisted private val preAuthController: PreAuthController,
) : ViewModel() {
    internal val state: StateFlow<SetPasswordUiState>
        field = MutableStateFlow(
            SetPasswordUiState(
                enabled = true,
                oldLockType = preAuthController.lockType,
                lockType = newLockType,
            )
        )

    fun setPassword(password: String) {
        state.update {
            it.copy(
                enabled = false,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                preAuthController.update(
                    lockType = newLockType,
                    newPassword = password,
                )

                state.update {
                    it.copy(
                        isUpdated = true,
                    )
                }
            } catch (e: Throwable) {
                state.update {
                    it.copy(
                        enabled = true,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun dismissError() {
        state.update {
            it.copy(
                error = null,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            newLockType: SecurityConfig.LockType,
            preAuthController: PreAuthController,
        ): SetPasswordViewModel
    }
}

internal data class SetPasswordUiState(
    val enabled: Boolean = false,
    val oldLockType: SecurityConfig.LockType,
    val lockType: SecurityConfig.LockType,
    val isUpdated: Boolean = false,
    val error: String? = null,
)