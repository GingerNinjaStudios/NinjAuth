import com.android.build.gradle.LibraryExtension
import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("ninjauth.android.lib")
                apply("ninjauth.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    // TODO set testInstrumentationRunner
                }
            }

            dependencies {
                add("implementation", project(":core:data"))

                add("implementation", findLibrary("androidx.hilt.navigation.compose"))
                add("implementation", findLibrary("androidx.lifecycle.runtime.ktx"))
                add("implementation", findLibrary("androidx.lifecycle.viewmodel.compose"))
                add("implementation", findLibrary("kotlinx.coroutines.android"))
            }
        }
    }
}