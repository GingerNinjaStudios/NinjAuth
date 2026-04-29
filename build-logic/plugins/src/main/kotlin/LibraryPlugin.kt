import com.android.build.api.dsl.LibraryExtension
import me.gingerninja.authenticator.SdkVersions
import me.gingerninja.authenticator.configureKotlinAndroid
import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = SdkVersions.target
                lint.targetSdk = SdkVersions.target

                defaultConfig.testInstrumentationRunner = "me.gingerninja.authenticator.core.testing.NinjAuthTestRunner"
            }

            configurations.configureEach {
                resolutionStrategy {
                    force(findLibrary("junit"))
                }
            }

            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }

}