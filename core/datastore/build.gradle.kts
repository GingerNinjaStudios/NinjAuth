plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.hilt)
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

    api(project(":core:common"))
    api(project(":core:model"))

    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:datastore-test"))
}