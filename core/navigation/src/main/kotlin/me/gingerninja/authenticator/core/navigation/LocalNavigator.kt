package me.gingerninja.authenticator.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalNavigator: ProvidableCompositionLocal<NinjAuthNavigator> =
    compositionLocalOf {
        error("No NinjAuthNavigator")
    }

val currentNavigator: NinjAuthNavigator
    @Composable
    @ReadOnlyComposable
    get() = LocalNavigator.current

@Composable
fun rememberNinjAuthNavigator(backStack: NavBackStack<NavKey>): NinjAuthNavigator =
    remember(backStack) { NinjAuthNavigatorImpl(backStack) }