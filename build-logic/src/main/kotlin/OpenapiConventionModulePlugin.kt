import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorGenerateExtension
import org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorValidateExtension
import javax.inject.Inject

public abstract class OpenapiConventionExtension @Inject constructor(objects: ObjectFactory) {
    public val outputDir: DirectoryProperty = objects.directoryProperty()
    public val packageName: Property<String> = objects.property(String::class.java)
    public val schemaDir: DirectoryProperty = objects.directoryProperty()
    public val schemaFilename: Property<String> = objects.property(String::class.java)

    public val groupId: Property<String> = objects.property(String::class.java)
    public val artifactId: Property<String> = objects.property(String::class.java)
    public val artifactVersion: Property<String> = objects.property(String::class.java)

    // Toggles
    public val cleanupOutput: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateApiDocumentation: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateApis: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateApiTests: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModelDocumentation: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModels: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModelTests: Property<Boolean> = objects.property(Boolean::class.java)
    public val validateSpec: Property<Boolean> = objects.property(Boolean::class.java)
}

internal class OpenapiConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        pluginManager.apply("org.openapi.generator")
        pluginManager.apply("org.springdoc.openapi-gradle-plugin")

        val extension = extensions.create("openapiConvention", OpenapiConventionExtension::class.java)

        pluginManager.withPlugin("org.openapi.generator") {
            project.afterEvaluate {
                configureDefaults(extension)
                configureOpenApiGenerate(extension)
                configureOpenApiValidate(extension)
                configureDependencies(libs)
                configureTasks(extension)
            }
        }
    }
}

private fun Project.configureDefaults(extension: OpenapiConventionExtension) {
    val cleanedName = project.name.replace(Regex("[-_]"), "")

    extension.outputDir.convention(layout.buildDirectory.dir("generated/sources/openapi"))
    extension.packageName.convention("${project.group}.$cleanedName")
    extension.schemaDir.convention(layout.projectDirectory.dir("src/api/schemas"))
    extension.schemaFilename.convention("${project.name}.yaml")

    extension.cleanupOutput.convention(true)
    extension.generateApiDocumentation.convention(true)
    extension.generateApis.convention(true)
    extension.generateApiTests.convention(true)
    extension.generateModelDocumentation.convention(true)
    extension.generateModels.convention(true)
    extension.generateModelTests.convention(true)
    extension.validateSpec.convention(true)
}

private fun Project.configureOpenApiGenerate(extension: OpenapiConventionExtension) {
    extensions.configure<OpenApiGeneratorGenerateExtension> {
        // Basic configuration
        generatorName.set("kotlin-spring")

        inputSpec.set(extension.schemaDir.file(extension.schemaFilename).map { it.asFile.absolutePath })
        outputDir.set(extension.outputDir.map { it.asFile.absolutePath })
        packageName.set(extension.packageName)

        // Generation control
        generateApiTests.set(extension.generateApiTests)
        generateModelTests.set(extension.generateModelTests)
        generateApiDocumentation.set(extension.generateApiDocumentation)
        generateModelDocumentation.set(extension.generateModelDocumentation)

        // Build info for generated projects
        groupId.set(project.group.toString())
        id.set("${project.name}-openapi")
        version.set(project.version.toString())

        val globalProps = project.providers.provider {
            fun toggle(v: Boolean) = if (v) "" else "false"
            mapOf(
                "apis" to toggle(extension.generateApis.getOrElse(true)),
                "models" to toggle(extension.generateModels.getOrElse(true)),
                "modelTests" to toggle(extension.generateModelTests.getOrElse(true)),
                "modelDocs" to toggle(extension.generateModelDocumentation.getOrElse(true)),
                "apiTests" to toggle(extension.generateApiTests.getOrElse(true)),
                "apiDocs" to toggle(extension.generateApiDocumentation.getOrElse(true)),
            )
        }
        globalProperties.set(globalProps)

        configOptions.set(
            mapOf(
                "interfaceOnly" to "true",
                "useSpringBoot3" to "true",
                "useSwaggerUI" to "false",
                "useTags" to "true",
                "library" to "spring-boot",
                "reactive" to "true",
            ),
        )

        // Additional properties for templates
        additionalProperties.set(
            mapOf(
                "title" to project.name,
                "packageVersion" to project.version.toString(),
            ),
        )
    }
}

private fun Project.configureOpenApiValidate(extension: OpenapiConventionExtension) {
    extensions.configure<OpenApiGeneratorValidateExtension> {
        inputSpec.set(extension.schemaDir.file(extension.schemaFilename).map { it.asFile.absolutePath })
        recommend.convention(true)
    }
}


private fun Project.configureDependencies(libs: VersionCatalog) {
    dependencies {
        // Enforce BOMs across common configurations if enabled
        val springdocOpenapiBom = libs.libraryOrThrow("springdoc-openapi-bom")
        val addBom: (String, Any) -> Unit = { conf, bom ->
            add(conf, enforcedPlatform(bom))
        }
        listOf("api", "implementation", "testImplementation").forEach { conf ->
            addBom(conf, springdocOpenapiBom)
        }

        add("implementation", libs.libraryOrThrow("springdoc-openapi-starter-webflux-ui"))
        add("implementation", libs.libraryOrThrow("springdoc-openapi-starter-webflux-api"))
    }
}

private fun Project.configureTasks(extension: OpenapiConventionExtension) {
    // Clean task for generated sources
    val cleanOpenApi = tasks.register<Delete>("cleanOpenApi") {
        group = "build"
        description = "Cleans generated OpenAPI sources"
        delete(extension.outputDir)
    }
    tasks.matching { it.name == "clean" }.configureEach {
        dependsOn(cleanOpenApi)
    }

    val openApiGenerate = tasks.named("openApiGenerate")

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.named("compileKotlin").configure {
            dependsOn(openApiGenerate)
        }
    }

    pluginManager.withPlugin("java") {
        tasks.named("compileJava").configure {
            dependsOn(openApiGenerate)
        }

        // Add generated sources to the main source set so they are compiled automatically
        extensions.configure<SourceSetContainer> {
            named("main") {
                java.srcDir(extension.outputDir.map { it.dir("src/main/java") })
                java.srcDir(extension.outputDir.map { it.dir("src/main/kotlin") })
            }
        }
    }

    // Run validation as part of 'check' only when enabled
    if (extension.validateSpec.orNull != false) {
        tasks.matching { it.name == "check" }.configureEach {
            dependsOn("openApiValidate")
        }
    }
}
