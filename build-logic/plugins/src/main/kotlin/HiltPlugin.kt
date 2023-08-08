import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dagger.hilt.android.plugin")
                apply("org.jetbrains.kotlin.kapt")
            }

            dependencies {
                "implementation"(findLibrary("hilt.android"))
                "kapt"(findLibrary("hilt.compiler"))
                "kaptAndroidTest"(findLibrary("hilt.compiler"))
            }

        }
    }

}