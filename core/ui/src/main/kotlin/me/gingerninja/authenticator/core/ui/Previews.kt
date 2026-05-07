package me.gingerninja.authenticator.core.ui

import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone - Light",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480"
)
@Preview(
    name = "Phone - Dark",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Phone Landscape - Light",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480,orientation=landscape"
)
@Preview(
    name = "Phone Landscape - Dark",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480,orientation=landscape",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Foldable - Light",
    group = "device",
    device = "spec:width=673dp,height=841dp,dpi=480",
)
@Preview(
    name = "Foldable - Dark",
    group = "device",
    device = "spec:width=673dp,height=841dp,dpi=480",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Tablet - Light",
    group = "device",
    device = "spec:width=1280dp,height=800dp,dpi=480"
)
@Preview(
    name = "Tablet - Dark",
    group = "device",
    device = "spec:width=1280dp,height=800dp,dpi=480",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
annotation class DevicePreviews