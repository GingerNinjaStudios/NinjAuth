plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
    id("ninjauth.android.room")
}

android {
    namespace = "me.gingerninja.authenticator.database"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.sqlcipher)
}