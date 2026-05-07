package me.gingerninja.authenticator.core.design.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.compose.ui.unit.dp
import me.gingerninja.authenticator.core.design.theme.NinjAuthTheme

class ThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        NinjAuthTheme {
            content()
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