plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    defaultConfig {
        testInstrumentationRunner = "me.gingerninja.authenticator.core.testing.NinjAuthTestRunner"
    }

    namespace = "me.gingerninja.authenticator.core.auth"
}

dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(project(":core:testing"))
}