pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
include(":core:data")
include(":core:database")
include(":core:model")

include(":feature:account")