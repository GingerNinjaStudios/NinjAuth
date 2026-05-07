package me.gingerninja.authenticator.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.currentNavigator

fun EntryProviderScope<NavKey>.authScreen() {
    entry<NinjaScreen.Auth> { screen ->
        val navigator = currentNavigator

        AuthScreen(
            isReauthenticating = screen.isReauthenticating,
            onAuthComplete = {
                navigator.popBackStack()
                navigator.navigate(NinjaScreen.Accounts)
            },
        )
    }
}