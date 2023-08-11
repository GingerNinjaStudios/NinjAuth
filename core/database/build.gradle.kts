plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
    id("ninjauth.android.room")
}

android {
    namespace = "me.gingerninja.authenticator.database"
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.sqlcipher)
    implementation(project(":core:model"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}