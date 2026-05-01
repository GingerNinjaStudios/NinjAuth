package me.gingerninja.authenticator.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NinjaScreen : NavKey {
    @Serializable
    data class Auth(val isReauthenticating: Boolean = false) : NinjaScreen

    @Serializable
    data object Accounts : NinjaScreen
}