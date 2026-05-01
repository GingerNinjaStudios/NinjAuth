@file:Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NinjAuth"
include(":app")
include(":core:auth")
include(":core:codegen")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:database-test")
include(":core:datastore")
include(":core:datastore-test")
include(":core:model")
include(":core:navigation")
include(":core:testing")
include(":core:ui")
include(":core:ui-design")

include(":feature:account")
include(":feature:auth")