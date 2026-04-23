plugins {
    alias(libs.plugins.ninjauth.android.lib)
    alias(libs.plugins.ninjauth.android.hilt)
	alias(libs.plugins.ninjauth.android.room)
}

android {
    namespace = "me.gingerninja.authenticator.core.database.test"
}

dependencies {
    api(project(":core:database"))
    api(libs.androidx.room.runtime)

    implementation(project(":core:common"))
    implementation(project(":core:testing"))
}