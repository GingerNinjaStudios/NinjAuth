import androidx.room.gradle.RoomExtension
import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("androidx.room")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas/")
            }

            dependencies {
                add("implementation", findLibrary("androidx.room.runtime"))
                add("implementation", findLibrary("androidx.room.ktx"))
                add("ksp", findLibrary("androidx.room.compiler"))
            }
        }
    }
}