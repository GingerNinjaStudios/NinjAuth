package me.gingerninja.authenticator.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.gingerninja.authenticator.core.design.anim.DefaultMotionAnimOffset
import me.gingerninja.authenticator.core.design.anim.motionEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionExitTransition
import me.gingerninja.authenticator.core.design.anim.motionPopEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionPopExitTransition
import me.gingerninja.authenticator.core.navigation.LocalNavigator
import me.gingerninja.authenticator.core.navigation.rememberNinjAuthNavigator
import me.gingerninja.authenticator.core.navigation.scene.BottomSheetSceneStrategy
import me.gingerninja.authenticator.feature.account.accountsScreen
import me.gingerninja.authenticator.feature.auth.authScreen
import me.gingerninja.authenticator.feature.settings.PreAuthNavEntryDecorator
import me.gingerninja.authenticator.feature.settings.settingsScreen


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NinjaNavDisplay(
    backStack: NavBackStack<NavKey>,
) {
    val navigator = rememberNinjAuthNavigator(backStack)

    val animOffset = DefaultMotionAnimOffset

    val viewModelStoreProvider = rememberViewModelStoreProvider()

    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ) {
        SharedTransitionLayout {
            NavDisplay(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(
                    remember { DialogSceneStrategy() },
                    remember { BottomSheetSceneStrategy() },
                    // TODO rememberListDetailSceneStrategy()
                ),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(viewModelStoreProvider),
                    remember { PreAuthNavEntryDecorator(viewModelStoreProvider, backStack) },
                ),
                entryProvider = entryProvider {
                    authScreen()
                    accountsScreen()
                    settingsScreen()
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
                predictivePopTransitionSpec = {
                    motionPopEnterTransition(animOffset) togetherWith motionPopExitTransition(
                        animOffset
                    )
                }
            )
        }
    }
}