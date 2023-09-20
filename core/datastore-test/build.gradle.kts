plugins {
    id("ninjauth.android.lib")
    id("ninjauth.android.hilt")
}

android {
    namespace = "me.gingerninja.authenticator.core.datastore.test"
}

dependencies {
    api(project(":core:datastore"))
    api(libs.androidx.datastore.preferences)

    implementation(project(":core:common"))
    implementation(project(":core:testing"))
}