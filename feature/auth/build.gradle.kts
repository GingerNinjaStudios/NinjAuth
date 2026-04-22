plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.lib.compose")
	id("ninjauth.android.feature")
}

android {
    namespace = "me.gingerninja.authenticator.feature.auth"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(project(":core:auth"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}