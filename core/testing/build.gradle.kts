plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.lib.compose")
    id("ninjauth.android.hilt")
}

android {
    namespace = "me.gingerninja.authenticator.core.testing"
}

dependencies {
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.androidx.test.core)
    api(libs.androidx.test.espresso.core)
    api(libs.androidx.test.ext.junit)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.runner)
    api(libs.hilt.android.testing)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)

    // we could use the regular kotlin dispatchers and tests but the docs at
    // https://developer.android.com/kotlin/flow/test#turbine recommend turbine for flows
    api(libs.turbine)

    debugApi(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.datetime)

    implementation(project(":core:data"))
    implementation(project(":core:model"))
}