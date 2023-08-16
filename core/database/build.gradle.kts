plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
    id("ninjauth.android.room")
}

android {
    defaultConfig {
        testInstrumentationRunner = "me.gingerninja.authenticator.core.testing.NinjAuthTestRunner"
    }

    namespace = "me.gingerninja.authenticator.core.database"
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.sqlcipher)
    implementation(project(":core:model"))

    androidTestImplementation(project(":core:testing"))
}