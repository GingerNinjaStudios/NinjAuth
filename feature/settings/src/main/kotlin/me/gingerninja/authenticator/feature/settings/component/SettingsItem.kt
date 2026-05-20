package me.gingerninja.authenticator.feature.settings.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.gingerninja.authenticator.core.design.component.NinjaModalBottomSheet

internal data class SettingsOption<T> private constructor(
    @field:StringRes val titleRes: Int? = null,
    val title: String? = null,
    val value: T,
) {
    constructor(title: String, value: T) : this(titleRes = null, title = title, value = value)

    constructor(@StringRes titleRes: Int, value: T) : this(
        titleRes = titleRes,
        title = null,
        value = value
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    SettingsItemImpl(
        modifier = modifier,
        title = title,
        value = value,
        onClick = {
            onClick?.invoke()
        },
        iconPainter = iconPainter,
        enabled = enabled,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SettingsItem(
    title: String,
    value: SettingsOption<T>?,
    onValueChange: (SettingsOption<T>) -> Unit,
    options: List<SettingsOption<T>>,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    enabled: Boolean = true,
) {
    var showingOptions by rememberSaveable { mutableStateOf(false) }

    SettingsItemImpl(
        modifier = modifier,
        title = title,
        value = value?.title ?: value?.titleRes?.let { stringResource(it) },
        onClick = {
            showingOptions = true
        },
        iconPainter = iconPainter,
        enabled = enabled,
    )

    if (showingOptions) {
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = options.indexOf(value)
        )

        NinjaModalBottomSheet(
            onDismissRequest = {
                showingOptions = false
            }
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            LazyColumn(
                modifier = Modifier.weight(1f, false),
                state = listState,
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(options) { option ->
                    ListItem(
                        onClick = {
                            showingOptions = false
                            onValueChange(option)
                        },
                        selected = option.value == value?.value,
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        )
                    ) {
                        Text(
                            text = option.title
                                ?: option.titleRes?.let { stringResource(option.titleRes) }
                                ?: "",
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsItem(
    title: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    enabled: Boolean = true,
) {
    SettingsItemImpl(
        modifier = modifier,
        title = title,
        value = value,
        onClick = {
            onCheckedChange(!checked)
        },
        iconPainter = iconPainter,
        enabled = enabled,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsItemImpl(
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        supportingContent = {
            Text(text = value ?: "")
        },
        leadingContent = {
            iconPainter?.let {
                Icon(
                    modifier = Modifier.size(IconSize),
                    painter = iconPainter,
                    contentDescription = null,
                )
            } ?: Box(
                modifier = Modifier.size(IconSize),
            )
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        contentPadding = SettingsItemContentPadding,
    ) {
        Text(text = title)
    }
}

private val IconSize = 24.dp

private val SettingsItemContentPadding =
    ListItemDefaults.ContentPadding.plus(PaddingValues(vertical = 4.dp))