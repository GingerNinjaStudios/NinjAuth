package me.gingerninja.authenticator.core.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import me.gingerninja.authenticator.core.design.theme.NinjAuthTheme
import me.gingerninja.authenticator.core.navigation.LocalNavigator
import me.gingerninja.authenticator.core.navigation.NinjAuthNavigator
import me.gingerninja.authenticator.core.navigation.NinjaScreen

class ThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalNavigator provides FakeNavigator) {
            NinjAuthTheme {
                LocalNavAnimatedContentScope
                content()
            }
        }
    }
}

class PreviewContainerWrapper : PreviewWrapperProvider {
    private val themeWrapper = ThemeWrapper()

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        themeWrapper.Wrap {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}

private object FakeNavigator : NinjAuthNavigator {
    override fun navigate(screen: NinjaScreen) {

    }

    override fun popBackStack() = true
    override fun popBackStack(
        screen: NinjaScreen,
        inclusive: Boolean,
    ): Boolean = true
}