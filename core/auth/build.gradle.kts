plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.lib.compose")
}

android {
    namespace = "me.gingerninja.authenticator.auth"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}