import com.android.build.gradle.TestExtension
import me.gingerninja.authenticator.SdkVersions
import me.gingerninja.authenticator.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.test")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<TestExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = SdkVersions.target
            }
        }
    }
}