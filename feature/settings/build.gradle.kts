plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
	alias(libs.plugins.ninjauth.android.feature)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "me.gingerninja.authenticator.feature.settings"
}

dependencies {
    implementation(projects.core.auth)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}