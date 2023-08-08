package me.gingerninja.authenticator

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion =
                libs.findVersion("androidx-compose-compiler").get().toString()
        }

        /* we use bleeding edge libs instead
        dependencies {
            val bom = findLibrary("androidx-compose-bom")
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
        }
        */
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters()
        }
    }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
    val params = mutableListOf<String>()
    val metricsFolder = File(project.buildDir, "compose_metrics")

    val enableMetrics =
        project.providers.gradleProperty("enableComposeCompilerMetrics").orNull == "true"
    if (enableMetrics) {
        params += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
        )
    }

    val enableReports =
        project.providers.gradleProperty("enableComposeCompilerReports").orNull == "true"
    if (enableReports) {
        params += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + metricsFolder.absolutePath
        )
    }

    return params.toList()
}