plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    namespace = "me.gingerninja.authenticator.codegen"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit)
}