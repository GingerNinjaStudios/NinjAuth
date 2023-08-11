plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    namespace = "me.gingerninja.authenticator.data"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(project(":core:database"))
    implementation(project(":core:model"))
}