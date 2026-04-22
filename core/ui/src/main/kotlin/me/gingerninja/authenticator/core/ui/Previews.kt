package me.gingerninja.authenticator.core.ui

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "phone",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480"
)
@Preview(
    name = "landscape",
    group = "device",
    device = "spec:width=411dp,height=891dp,dpi=480,orientation=landscape"
)
@Preview(
    name = "foldable",
    group = "device",
    device = "spec:width=673dp,height=841dp,dpi=480"
)
@Preview(
    name = "tablet",
    group = "device",
    device = "spec:width=1280dp,height=800dp,dpi=480"
)
annotation class DevicePreviews