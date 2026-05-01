plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
    alias(libs.plugins.ninjauth.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "me.gingerninja.authenticator.core.navigation"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    
    implementation(libs.kotlinx.serialization.json)
}