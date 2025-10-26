/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("org.openapi.generator")
            pluginManager.apply("org.springdoc.openapi-gradle-plugin")

            val libs = getLibs()
            val extension = extensions.create("openapiConvention", OpenapiConventionExtension::class.java)

            afterEvaluate {
                sanitizeConfig(extension)
                configureOpenApiGenerate(extension, libs)
                configureOpenApiTasks(extension, libs)
            }

            afterEvaluate {
                println("==================================")
                println("OpenAPI Conventions Applied:")
                println(" - Schema Directory: ${extension.schemaDir.get()}")
                println(" - Schema Filename: ${extension.schemaFilename.get()}")
                println(" - Output Directory: ${extension.outputDir.get()}")
                println(" - Package Name: ${extension.packageName.get()}")
                println(" - Artifact Version: ${extension.artifactVersion.get()}")
                println(" - Group ID: ${extension.groupId.get()}")
                println(" - Generate APIs: ${extension.generateApis.get()}")
                println(" - Generate Models: ${extension.generateModels.get()}")
                println(" - Validate Spec: ${extension.validateSpec.get()}")
                println(" - Recommend Fixes: ${extension.recommend.get()}")
                println("==================================")
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

private fun Project.configureOpenApiGenerate(
    extension: OpenapiConventionExtension,
    libs: VersionCatalog,
) {
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
        outputDir.set(
            extension.outputDir
                .get()
                .asFile.absolutePath,
        )
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
                "apis" to if (extension.generateApis.get()) "true" else "",
                "models" to if (extension.generateModels.get()) "true" else "",
            ),
        )

        configOptions.set(
            mapOf(
                "useSpringBoot3" to "true",
                "useSwaggerUI" to "false",
                "useTags" to "true",
                "library" to "spring-boot",
                "reactive" to "true",
                "delegatePattern" to "true",
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

private fun Project.configureOpenApiTasks(
    extension: OpenapiConventionExtension,
    libs: VersionCatalog,
) {
    // Clean task for generated sources
    val cleanOpenApi =
        tasks.register<Delete>("cleanOpenApi") {
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
            if (!extension.schemaDir
                    .get()
                    .asFile
                    .exists()
            ) {
                logger.warn("OpenAPI schema directory does not exist: ${extension.schemaDir.get()}")
            }
        }
    }

    tasks.named("check") {
        dependsOn(tasks.named("openApiValidate"))
    }
}
