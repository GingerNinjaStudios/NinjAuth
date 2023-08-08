package me.gingerninja.authenticator

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Sets the SDK levels. This is used by multiple plugins as well.
 */
internal object SdkVersions {
    const val compile = 34
    const val target = 34
    const val min = 26
}

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = SdkVersions.compile

        defaultConfig {
            minSdk = SdkVersions.min
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }

    configureKotlin()

    dependencies {
        add("coreLibraryDesugaring", findLibrary("android.desugarJdkLibs"))
    }
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()

            // if warningsAsErrors=true in .gradle/gradle.properties is set, warnings will become errors
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                // "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }
}