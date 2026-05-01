import com.android.build.api.dsl.LibraryExtension
import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

class FeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("ninjauth.android.lib")
                apply("ninjauth.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner =
                        "me.gingerninja.authenticator.core.testing.NinjAuthTestRunner"
                }
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:datastore"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:ui-design"))
                //add("implementation", project(":core:domain"))

                add("implementation", findLibrary("androidx.hilt.navigation.compose"))
                add("implementation", findLibrary("androidx.hilt.lifecycle.viewmodel.compose"))
                add("implementation", findLibrary("androidx.lifecycle.runtime.compose"))
                add("implementation", findLibrary("androidx.lifecycle.runtime.ktx"))
                add("implementation", findLibrary("androidx.lifecycle.viewmodel.compose"))
                add("implementation", findLibrary("kotlinx.coroutines.android"))

                add("testImplementation", kotlin("test"))
                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", kotlin("test"))
                add("androidTestImplementation", project(":core:testing"))
            }
        }
    }
}