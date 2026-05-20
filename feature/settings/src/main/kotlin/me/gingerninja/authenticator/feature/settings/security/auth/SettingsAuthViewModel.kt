package me.gingerninja.authenticator.feature.settings.security.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import me.gingerninja.authenticator.core.auth.password.PasswordAuthenticator
import me.gingerninja.authenticator.core.datastore.NinjAuthSettings
import me.gingerninja.authenticator.core.model.settings.SecurityConfig
import me.gingerninja.authenticator.feature.settings.component.PreAuthController
import javax.inject.Inject

@HiltViewModel
class SettingsAuthViewModel @Inject constructor(
    settings: NinjAuthSettings,
    private val authenticator: PasswordAuthenticator,
) : ViewModel() {
    internal val preAuthController = settings.data.value.let { settings ->
        PreAuthController(
            scope = viewModelScope,
            passwordAuthenticator = authenticator,
            initialLockType = settings.security.lockType,
        )
    }
}

internal data class PreAuthUiState(
    val enabled: Boolean = false,
    val lockType: SecurityConfig.LockType,
    val authError: PreAuthError? = null,
    val isAuthenticated: Boolean = false,
)

internal data class PreAuthError(
    val wrongPass: Boolean
)