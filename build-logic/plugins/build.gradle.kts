import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "me.gingerninja.authenticator.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.androidx.room.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("appCompose") {
            id = libs.plugins.ninjauth.android.app.compose.get().pluginId
            implementationClass = "AppComposePlugin"
        }

        register("app") {
            id = libs.plugins.ninjauth.android.app.asProvider().get().pluginId
            implementationClass = "AppPlugin"
        }

        register("libCompose") {
            id = libs.plugins.ninjauth.android.lib.compose.get().pluginId
            implementationClass = "LibraryComposePlugin"
        }

        register("lib") {
            id = libs.plugins.ninjauth.android.lib.asProvider().get().pluginId
            implementationClass = "LibraryPlugin"
        }

        register("feature") {
            id = libs.plugins.ninjauth.android.feature.get().pluginId
            implementationClass = "FeaturePlugin"
        }

        register("hilt") {
            id = libs.plugins.ninjauth.android.hilt.get().pluginId
            implementationClass = "HiltPlugin"
        }

        register("room") {
            id = libs.plugins.ninjauth.android.room.get().pluginId
            implementationClass = "RoomPlugin"
        }

        register("androidTest") {
            id = libs.plugins.ninjauth.android.test.get().pluginId
            implementationClass = "TestPlugin"
        }
    }
}