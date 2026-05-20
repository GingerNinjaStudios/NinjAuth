package me.gingerninja.authenticator.core.design.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun NinjaMessageCard(
    text: String,
    modifier: Modifier = Modifier,
    colors: NinjaMessageCardColors = NinjaMessageCardDefaults.colors(),
    contentPadding: PaddingValues = NinjaMessageCardDefaults.ContentPadding,
    @DrawableRes icon: Int? = null,
) {
    /*NinjaMessageCard(
        modifier = modifier,
        text = text,
        colors = colors,
        contentPadding = contentPadding,
        iconContent = icon?.let {
            @Composable {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = colors.iconColor,
                )
            }
        },
    )*/

    NinjaMessageCard(
        modifier = modifier,
        colors = colors,
    ) {
        MessageRow(
            text = text,
            contentPadding = contentPadding,
            icon = icon,
        )
    }
}

@Composable
fun NinjaMessageCard(
    modifier: Modifier = Modifier,
    colors: NinjaMessageCardColors = NinjaMessageCardDefaults.colors(),
    content: @Composable NinjaMessageCardScope.() -> Unit,
) {
    val scope = remember(colors) {
        NinjaMessageCardScopeImpl(colors)
    }

    Surface(
        modifier = modifier,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.borderColor,
        ),
    ) {
        scope.content()
    }
    /*NinjaMessageCard(
        modifier = modifier,
        text = text,
        colors = colors,
        contentPadding = contentPadding,
        iconContent = icon?.let {
            @Composable {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )*/
}

@Composable
fun NinjaMessageCardScope.MessageRow(
    text: String,
    contentPadding: PaddingValues = NinjaMessageCardDefaults.ContentPadding,
    icon: Int? = null,
) {
    Row(
        modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.iconColor,
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

interface NinjaMessageCardScope {
    val colors: NinjaMessageCardColors
}

internal class NinjaMessageCardScopeImpl(
    override val colors: NinjaMessageCardColors,
) : NinjaMessageCardScope {

}

object NinjaMessageCardDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor: Color = contentColorFor(containerColor),
        iconColor: Color = MaterialTheme.colorScheme.primary,
        borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    ) = NinjaMessageCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        borderColor = borderColor,
    )

    @Composable
    fun errorColors() = colors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        borderColor = MaterialTheme.colorScheme.error,
        iconColor = MaterialTheme.colorScheme.error,
    )

    val ContentPadding = PaddingValues(16.dp)
}

class NinjaMessageCardColors(
    val containerColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val borderColor: Color,
)