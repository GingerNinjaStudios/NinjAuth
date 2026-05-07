package me.gingerninja.authenticator.feature.account

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import me.gingerninja.authenticator.core.navigation.NinjaScreen

fun EntryProviderScope<NavKey>.accountsScreen() {
    entry<NinjaScreen.Accounts> {
        AccountsScreen()
    }
}