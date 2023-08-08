import com.google.devtools.ksp.gradle.KspExtension
import me.gingerninja.authenticator.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

class RoomPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<KspExtension> {
                arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
            }

            dependencies {
                add("implementation", findLibrary("androidx.room.runtime"))
                add("implementation", findLibrary("androidx.room.ktx"))
                add("ksp", findLibrary("androidx.room.compiler"))
            }
        }
    }

    class RoomSchemaArgProvider(
        @get:InputDirectory
        @get:PathSensitive(PathSensitivity.RELATIVE)
        val schemaDir: File,
    ) : CommandLineArgumentProvider {
        override fun asArguments() = listOf("room.schemaLocation=${schemaDir.path}")
    }
}