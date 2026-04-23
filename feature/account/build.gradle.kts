plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.lib.compose)
	alias(libs.plugins.ninjauth.android.feature)
}

android {
    namespace = "me.gingerninja.authenticator.feature.account"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}