package me.gingerninja.authenticator.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import me.gingerninja.authenticator.core.navigation.NinjaScreen
import me.gingerninja.authenticator.core.navigation.PreAuthMetadata
import me.gingerninja.authenticator.core.navigation.PreAuthRequired
import me.gingerninja.authenticator.feature.settings.security.auth.SettingsAuthViewModel
import me.gingerninja.authenticator.feature.settings.security.biometric.SettingsBiometricScreen
import me.gingerninja.authenticator.feature.settings.security.lock.SettingsLockTypeScreen
import me.gingerninja.authenticator.feature.settings.security.password.SetPasswordScreen

fun EntryProviderScope<NavKey>.settingsScreen() {
    entry<NinjaScreen.Settings.Main> {
        SettingsScreen()
    }

    /*entry<NinjaScreen.Settings.PreAuth> { screen ->
        val navigator = currentNavigator

        SettingsAuthScreen(
            onConfirm = { password ->
                val target = screen.target.createNavKey(password)

                navigator.popBackStack()
                navigator.navigate(target)
            }
        )
    }*/

    /*entry<NinjaScreen.Settings.PasswordSet>{
        SettingsPasswordSetScreen()
    }*/

    entry<NinjaScreen.Settings.LockTypeChooser>(
        metadata = metadata {
            put(PreAuthMetadata.ViewModelKey, PreAuthMetadata.VIEW_MODEL_VALUE)
        },
    ) { screen ->
        SettingsLockTypeScreen(
            //lockType = screen.lockType,
        )
    }

    entry<NinjaScreen.Settings.Biometric>(
        metadata = metadata {
            put(PreAuthMetadata.ViewModelKey, PreAuthMetadata.VIEW_MODEL_VALUE)
        },
    ) { screen ->
        SettingsBiometricScreen(
            //lockType = screen.lockType,
        )
    }

    entry<NinjaScreen.Settings.PasswordSet>(
        metadata = metadata {
            put(PreAuthMetadata.ViewModelKey, PreAuthMetadata.VIEW_MODEL_VALUE)
        },
    ) { screen ->
        SetPasswordScreen(
            lockType = screen.lockType,
        )
    }
}

class PreAuthNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider,
    backStack: NavBackStack<NavKey>,
) : NavEntryDecorator<T>(
    onPop = {
        if (backStack.none { it is PreAuthRequired }) {
            viewModelStoreProvider.clearKey(PreAuthMetadata.VIEW_MODEL_VALUE)
        }
    },
    decorate = { entry ->
        val sharedVmKey = entry.metadata[PreAuthMetadata.ViewModelKey]

        if (sharedVmKey == PreAuthMetadata.VIEW_MODEL_VALUE) {
            val owner = rememberViewModelStoreOwner(
                key = sharedVmKey,
                provider = viewModelStoreProvider,
                savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
            )

            CompositionLocalProvider(LocalPreAuthViewModelStoreOwner provides owner) {
                entry.Content()
            }
        } else {
            entry.Content()
        }
    }
)

internal val LocalPreAuthViewModelStoreOwner =
    staticCompositionLocalOf<ViewModelStoreOwner> { error("No LocalPreAuthViewModelStoreOwner provided") }

@Composable
internal fun getAuthViewModel(): SettingsAuthViewModel {
    return hiltViewModel<SettingsAuthViewModel>(
        viewModelStoreOwner = LocalPreAuthViewModelStoreOwner.current,
    )
}

@Composable
internal fun getPreAuthController() = getAuthViewModel().preAuthController