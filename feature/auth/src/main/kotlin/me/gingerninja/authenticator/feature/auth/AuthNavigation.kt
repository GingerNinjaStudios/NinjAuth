package me.gingerninja.authenticator.feature.auth

import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.authScreen(
    modifier: Modifier = Modifier,
    onAuthComplete: () -> Unit,
) {
    composable("auth?${NavArgs.isReauth}={${NavArgs.isReauth}}", arguments = listOf(
        navArgument(NavArgs.isReauth) {
            type = NavType.BoolType
            defaultValue = false
            nullable = false
        }
    )) {
        AuthScreen(
            modifier = modifier,
            onAuthComplete = onAuthComplete
        )
    }
}

internal data class AuthArgs(
    val isReauthenticating: Boolean
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        isReauthenticating = requireNotNull(savedStateHandle[NavArgs.isReauth]) as Boolean
    )
}

internal object NavArgs {
    const val isReauth = "reauth"
}

fun NavController.navigateToAuth(isReauthenticating: Boolean = false) {
    navigate("auth?${NavArgs.isReauth}=$isReauthenticating") {
        popUpTo("auth"){
            inclusive = true
        }
        //launchSingleTop = true
    }
}