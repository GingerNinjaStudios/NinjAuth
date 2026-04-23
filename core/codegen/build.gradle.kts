plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.hilt)
}

android {
    namespace = "me.gingerninja.authenticator.core.codegen"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(project(":core:model"))

    testImplementation(libs.junit)
}