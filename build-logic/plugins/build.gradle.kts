import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "me.gingerninja.authenticator.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("appCompose") {
            id = "ninjauth.android.app.compose"
            implementationClass = "AppComposePlugin"
        }

        register("app") {
            id = "ninjauth.android.app"
            implementationClass = "AppPlugin"
        }

        register("libCompose") {
            id = "ninjauth.android.lib.compose"
            implementationClass = "LibraryComposePlugin"
        }

        register("lib") {
            id = "ninjauth.android.lib"
            implementationClass = "LibraryPlugin"
        }

        register("feature") {
            id = "ninjauth.android.feature"
            implementationClass = "FeaturePlugin"
        }

        register("hilt") {
            id = "ninjauth.android.hilt"
            implementationClass = "HiltPlugin"
        }

        register("room") {
            id = "ninjauth.android.room"
            implementationClass = "RoomPlugin"
        }

        register("androidTest") {
            id = "ninjauth.android.test"
            implementationClass = "TestPlugin"
        }
    }
}