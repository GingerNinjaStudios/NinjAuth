package me.gingerninja.authenticator.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.gingerninja.authenticator.core.navigation.LocalNavigator
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.rememberNinjAuthNavigator
import me.gingerninja.authenticator.feature.auth.AuthScreen
import me.gingerninja.authenticator.feature.auth.authScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NinjaNavDisplay(
) {
    val backStack = rememberNavBackStack(NinjaScreen.Auth())
    val navigator = rememberNinjAuthNavigator(backStack)

    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategies = listOf(
                remember { DialogSceneStrategy() },
                // TODO rememberListDetailSceneStrategy()
            ),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                //authScreen()
                entry<NinjaScreen.Auth> { screen ->
                    AuthScreen(
                        isReauthenticating = screen.isReauthenticating,
                        onAuthComplete = {
                            navigator.popBackStack()
                            navigator.navigate(NinjaScreen.Accounts)
                        },
                    )
                }

                entry<NinjaScreen.Accounts> { screen ->

                }
            },
        )
    }
}