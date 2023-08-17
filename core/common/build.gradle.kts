plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    namespace = "me.gingerninja.authenticator.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
	
    implementation(project(":core:testing"))
}