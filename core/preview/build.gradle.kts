plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
}

android {
    namespace = "me.gingerninja.authenticator.core.preview"
}

dependencies {
    implementation(projects.core.uiDesign)
    implementation(projects.core.navigation)
    
    api(libs.androidx.compose.ui.tooling.preview)

    debugApi(libs.androidx.compose.ui.tooling)

    androidTestImplementation(project(":core:testing"))
}