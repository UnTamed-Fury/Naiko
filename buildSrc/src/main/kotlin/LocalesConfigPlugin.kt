import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

private val emptyResourcesElement = "<resources>\\s*</resources>|<resources\\s*/>".toRegex()

fun Project.registerLocalesConfigTask(outputResourceDir: File): TaskProvider<Task> {
    return tasks.register("generateLocalesConfig") {
        val languages = fileTree("$projectDir/src/commonMain/moko-resources/")
            .matching { include("**/strings.xml") }
            .filterNot { it.readText().contains(emptyResourcesElement) }
            .map {
                it.parentFile.name
                    .replace("base", "en")
                    .replace("-r", "-")
                    .replace("+", "-")
                    .takeIf(String::isNotBlank) ?: "en"
            }
            .sorted()
            .joinToString(separator = "\n") {
                "   <locale android:name=\"$it\"/>"
            }

        val content = """
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
$languages
</locale-config>
""".trimIndent()

        outputResourceDir.resolve("xml/locales_config.xml").apply {
            parentFile.mkdirs()
            writeText(content)
        }
    }
}
