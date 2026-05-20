package me.gingerninja.authenticator.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

interface NinjAuthNavigator {
    fun navigate(screen: NinjaScreen)

    fun popBackStack(): Boolean

    fun popBackStack(screen: NinjaScreen, inclusive: Boolean = false): Boolean
}

internal class NinjAuthNavigatorImpl(
    private val backStack: NavBackStack<NavKey>,
) : NinjAuthNavigator {

    override fun navigate(screen: NinjaScreen) {
        backStack.add(screen)
    }

    override fun popBackStack(): Boolean {
        return backStack.removeLastOrNull() != null
    }

    override fun popBackStack(
        screen: NinjaScreen,
        inclusive: Boolean
    ): Boolean {
        val listToDrop = backStack.takeLastWhile { it != screen }

        return backStack.removeAll(listToDrop).apply {
            if (inclusive && backStack.isNotEmpty()) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    }
}