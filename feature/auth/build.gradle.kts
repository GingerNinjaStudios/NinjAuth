plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
	alias(libs.plugins.ninjauth.android.feature)
}

android {
    namespace = "me.gingerninja.authenticator.feature.auth"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(project(":core:auth"))
    implementation(libs.androidx.biometric.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}