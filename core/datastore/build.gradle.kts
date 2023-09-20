plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    defaultConfig {
        testInstrumentationRunner = "me.gingerninja.authenticator.core.testing.NinjAuthTestRunner"
    }

    namespace = "me.gingerninja.authenticator.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:testing"))
    androidTestImplementation(project(":core:datastore-test"))
}