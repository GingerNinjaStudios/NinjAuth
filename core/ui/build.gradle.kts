plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
}

android {
    namespace = "me.gingerninja.authenticator.core.ui"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material3)
    //api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.tooling.preview)
    //api(libs.androidx.compose.ui.util)
    //api(libs.androidx.metrics)
    //api(libs.androidx.tracing.ktx)

    debugApi(libs.androidx.compose.ui.tooling)

    //implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui-design"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.datetime)

    androidTestImplementation(project(":core:testing"))
}