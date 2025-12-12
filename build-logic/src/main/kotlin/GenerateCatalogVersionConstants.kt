import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale.getDefault

public abstract class GenerateCatalogVersionConstants : DefaultTask() {

    @get:Input
    public abstract val versionAliases: ListProperty<String>

    @get:Input
    public abstract val pluginAliases: ListProperty<String>

    @get:Input
    public abstract val libraryAliases: ListProperty<String>

    @get:Input
    public abstract val bundleAliases: ListProperty<String>

    @get:Input
    public abstract val namespace: Property<String>

    @get:Input
    public abstract val className: Property<String>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @TaskAction
    public fun generate() {
        val versionAliases = versionAliases.get()
        val pluginAliases = pluginAliases.get()
        val libraryAliases = libraryAliases.get()
        val bundleAliases = bundleAliases.get()
        val className = className.get()
        val outputDir = outputDir.get().asFile
        val namespace = namespace.get()

        logger.debug(
            "Generating catalog constants in $outputDir for "
                    + "version: ${versionAliases.size}"
                    + ", plugins: ${pluginAliases.size}"
                    + ", libraries: ${libraryAliases.size}"
                    + ", bundles: ${bundleAliases.size}",
        )

        val versions = versionAliases.joinToString("\n") {
            "        public const val ${it.toConstantName()}: String = \"$it\""
        }

        val plugins = pluginAliases.joinToString("\n") {
            "        public const val ${it.toConstantName()}: String = \"$it\""
        }

        val libraries = libraryAliases.joinToString("\n") {
            "        public const val ${it.toConstantName()}: String = \"$it\""
        }

        val bundles = bundleAliases.joinToString("\n") {
            "        public const val ${it.toConstantName()}: String = \"$it\""
        }

        val content = """
            |// Auto-generated file. Do not modify.
            |package $namespace
            |
            |public object $className {
            |
            |    public object Versions {
            |$versions
            |    }
            |
            |    public object Plugins {
            |$plugins
            |    }
            |
            |    public object Libraries {
            |$libraries
            |    }
            |
            |    public object Bundles {
            |$bundles
            |    }
            |}
            |
        """.trimMargin()

        val outputFile = File(outputDir, "$className.kt")
        outputFile.parentFile.mkdirs()

        println(outputFile.absoluteFile)

        outputFile.writeText(content)

        logger.debug("Generated version catalog constants at: ${outputFile.absolutePath}")
    }
}

private fun String.toConstantName(): String {
    return this.replace("-", "_").replace(".", "_").uppercase(getDefault())
}
