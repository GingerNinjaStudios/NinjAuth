plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.hilt)
}

android {
    namespace = "me.gingerninja.authenticator.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
	
    testImplementation(project(":core:testing"))
}