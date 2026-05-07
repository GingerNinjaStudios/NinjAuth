package me.gingerninja.authenticator.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.gingerninja.authenticator.core.design.anim.motionEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionExitTransition
import me.gingerninja.authenticator.core.design.anim.motionPopEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionPopExitTransition
import me.gingerninja.authenticator.core.navigation.LocalNavigator
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.rememberNinjAuthNavigator
import me.gingerninja.authenticator.core.navigation.scene.BottomSheetSceneStrategy
import me.gingerninja.authenticator.feature.account.accountsScreen
import me.gingerninja.authenticator.feature.auth.AuthScreen
import me.gingerninja.authenticator.feature.auth.authScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NinjaNavDisplay(
) {
    val backStack = rememberNavBackStack(NinjaScreen.Auth())
    val navigator = rememberNinjAuthNavigator(backStack)

    val animOffset = with(LocalDensity.current) {
        30.dp.roundToPx()
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ) {
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(
                    remember { DialogSceneStrategy() },
                    remember { BottomSheetSceneStrategy() },
                    // TODO rememberListDetailSceneStrategy()
                ),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    authScreen()
                    accountsScreen()
                    /*entry<NinjaScreen.Auth> { screen ->
                        AuthScreen(
                            isReauthenticating = screen.isReauthenticating,
                            onAuthComplete = {
                                navigator.popBackStack()
                                navigator.navigate(NinjaScreen.Accounts)
                            },
                        )
                    }*/

                    /*entry<NinjaScreen.Accounts> { screen ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondary),
                        )
                    }*/
                },
                sharedTransitionScope = this,
                transitionSpec = {
                    motionEnterTransition(animOffset) togetherWith motionExitTransition(animOffset)
                },
                popTransitionSpec = {
                    motionPopEnterTransition(animOffset) togetherWith motionPopExitTransition(
                        animOffset
                    )
                },
            )
        }
    }
}