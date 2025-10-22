import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorGenerateExtension
import org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorValidateExtension

internal class OpenapiConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.openapi.generator")
        pluginManager.apply("org.springdoc.openapi-gradle-plugin")

        val libs = getLibs()
        val extension = extensions.create("openapiConvention", OpenapiConventionExtension::class.java)

        afterEvaluate {
            sanitizeConfig(extension)
            configureOpenApiGenerate(extension, libs)
            configureOpenApiTasks(extension, libs)
        }
    }
}

private fun Project.sanitizeConfig(extension: OpenapiConventionExtension) {
    // Set default options with better naming and validation
    extension.outputDir.convention(layout.buildDirectory.dir("generated/sources/openapi/"))
    extension.schemaDir.convention(layout.projectDirectory.dir("src/api/schemas/"))

    // Set default generation flags
    extension.generateApis.convention(true)
    extension.generateModels.convention(true)
    extension.generateApiTests.convention(false)
    extension.generateModelTests.convention(false)
    extension.generateApiDocumentation.convention(false)
    extension.generateModelDocumentation.convention(false)
    extension.validateSpec.convention(true)
    extension.recommend.convention(true)

    require(
        extension.artifactVersion.isPresent && extension.artifactVersion.get().isNotBlank(),
    ) { "openapiConvention.artifactVersion must be set and non-empty" }
    require(
        extension.groupId.isPresent && extension.groupId.get().isNotBlank(),
    ) { "openapiConvention.groupId must be set and non-empty" }
    require(extension.outputDir.isPresent) { "openapiConvention.outputDir must be set" }
    require(
        extension.packageName.isPresent && extension.packageName.get().isNotBlank(),
    ) { "openapiConvention.packageName must be set and non-empty" }
    require(extension.schemaDir.isPresent) { "openapiConvention.schemaDir must be set" }
    require(
        extension.schemaFilename.isPresent && extension.schemaFilename.get().isNotBlank(),
    ) { "openapiConvention.schemaFilename must be set and non-empty" }
}

private fun Project.configureOpenApiGenerate(extension: OpenapiConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", platform(libs.libraryOrThrow("springdoc-openapi-bom")))
        add("implementation", libs.libraryOrThrow("springdoc-openapi-starter-webflux-ui"))
        add("implementation", libs.libraryOrThrow("springdoc-openapi-starter-webflux-api"))
    }

    val generatedInputSpec =
        extension.schemaDir.zip(extension.schemaFilename) { dir, filename -> dir.file(filename).asFile.absolutePath }

    // Configure the OpenAPI generator
    extensions.configure<OpenApiGeneratorGenerateExtension> {
        generatorName.set("kotlin-spring")
        inputSpec.set(generatedInputSpec)
        outputDir.set(extension.outputDir.get().asFile.absolutePath)
        packageName.set(extension.packageName)
        id.set(extension.packageName)

        // Generation control
        generateApiTests.set(extension.generateApiTests)
        generateModelTests.set(extension.generateModelTests)
        generateApiDocumentation.set(extension.generateApiDocumentation)
        generateModelDocumentation.set(extension.generateModelDocumentation)

        // Build info
        groupId.set(extension.groupId)
        version.set(extension.artifactVersion)

        globalProperties.set(
            mapOf(
                "apis" to extension.generateApis.get().toString(),
                "models" to extension.generateModels.get().toString(),
            ),
        )

        configOptions.set(
            mapOf(
                "interfaceOnly" to "true",
                "useSpringBoot3" to "true",
                "useSwaggerUI" to "false",
                "useTags" to "true",
                "library" to "spring-boot",
                "reactive" to "true",
                "documentationProvider" to "springdoc",
                "useBeanValidation" to "true",
                "useJakartaEe" to "true",
                "serializationLibrary" to "jackson",
                "serializableModel" to "true",
                "skipDefaultInterface" to "true",
                "exceptionHandler" to "false",
            ),
        )
    }

    // Configure validation
    extensions.configure<OpenApiGeneratorValidateExtension> {
        inputSpec.set(generatedInputSpec)
        recommend.set(extension.recommend)
    }

    // Add generated sources to the main source set so they are compiled automatically
    extensions.configure<SourceSetContainer> {
        named("main") {
            java.srcDir(extension.outputDir.get().file("src/main/java"))
            java.srcDir(extension.outputDir.get().file("src/main/kotlin"))
        }
    }
}

private fun Project.configureOpenApiTasks(extension: OpenapiConventionExtension, libs: VersionCatalog) {
    // Clean task for generated sources
    val cleanOpenApi = tasks.register<Delete>("cleanOpenApi") {
        group = "build"
        description = "Cleans generated OpenAPI sources"
        delete(extension.outputDir)
    }
    tasks.named("clean").configure { dependsOn(cleanOpenApi) }

    // Wire the generator task into the compilation lifecycle
    val openApiGenerateTask = tasks.named("openApiGenerate")
    tasks.withType<KotlinCompile>().configureEach {
        dependsOn(openApiGenerateTask)
    }
    tasks.withType<JavaCompile>().configureEach {
        dependsOn(openApiGenerateTask)
    }

    tasks.named("openApiValidate") {
        enabled = extension.validateSpec.get()
        inputs.property("recommend", extension.recommend.get())

        doFirst {
            if (!extension.schemaDir.get().asFile.exists()) {
                logger.warn("OpenAPI schema directory does not exist: ${extension.schemaDir.get()}")
            }
        }
    }

    tasks.named("check") {
        dependsOn(tasks.named("openApiValidate"))
    }

    tasks.register("openApiConfig") {
        group = "documentation"
        description = "Shows the current OpenAPI configuration"

        doLast {
            logger.lifecycle("OpenAPI Configuration:")
            logger.lifecycle("  Schema: ${extension.schemaDir.get().file(extension.schemaFilename.get())}")
            logger.lifecycle("  Output: ${extension.outputDir.get()}")
            logger.lifecycle("  Package: ${extension.packageName.get()}")
            logger.lifecycle("  Generate APIs: ${extension.generateApis.get()}")
            logger.lifecycle("  Generate Models: ${extension.generateModels.get()}")
        }
    }
}
