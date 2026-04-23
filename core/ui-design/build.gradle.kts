plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
}

android {
    namespace = "me.gingerninja.authenticator.core.ui.design"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material3)
    //api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.tooling.preview)
    //api(libs.androidx.compose.ui.util)

    debugApi(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)

    androidTestImplementation(project(":core:testing"))
}