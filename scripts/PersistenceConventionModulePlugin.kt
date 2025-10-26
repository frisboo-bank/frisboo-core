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
import org.gradle.kotlin.dsl.dependencies

internal class PersistenceConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            val libs = getLibs()

            val extension = extensions.create("persistenceConventions", PersistenceConventionExtension::class.java)

            afterEvaluate {
                if (extension.useMongo.get()) {
                    configureMongo(extension, libs)
                }
                if (extension.usePostgres.get()) {
                    configurePostgres(extension, libs)
                }
            }

            afterEvaluate {
                println("==================================")
                println("Persistence Conventions Applied:")
                println(" - Use MongoDB: ${extension.useMongo.get()}")
                println(" - Use PostgreSQL: ${extension.usePostgres.get()}")
                println(" - Use Migration: ${extension.useMigration.get()}")
                println(" - Use Testcontainers: ${extension.useTestcontainers.get()}")
                println("==================================")
            }
        }
}

private fun Project.configureMongo(
    extension: PersistenceConventionExtension,
    libs: VersionCatalog,
) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-boot-starter-data-mongodb"))
        add("implementation", libs.libraryOrThrow("mongodb"))

        if (extension.useTestcontainers.get()) {
            // Align Testcontainers modules if BOM alias exists
            libs.findLibrary("testcontainers-bom").ifPresent { bom ->
                add("testImplementation", platform(bom.get()))
            }

            add("testImplementation", libs.libraryOrThrow("testcontainers-mongodb"))

            libs.findLibrary("testcontainers-junit-jupiter").ifPresent {
                add("testImplementation", it.get())
            }
        }
    }
}

private fun Project.configurePostgres(
    extension: PersistenceConventionExtension,
    libs: VersionCatalog,
) {
    dependencies {
        add("implementation", platform(libs.libraryOrThrow("exposed-bom")))

        add("implementation", libs.libraryOrThrow("exposed-spring-boot-starter"))
        add("implementation", libs.libraryOrThrow("exposed-jdbc"))
        add("implementation", libs.libraryOrThrow("exposed-kotlin-datetime"))
        add("implementation", libs.libraryOrThrow("h2"))
        add("runtimeOnly", libs.libraryOrThrow("postgresql"))

        if (extension.useMigration.get()) {
            add("implementation", libs.libraryOrThrow("flyway-core"))
            add("implementation", libs.libraryOrThrow("flyway-postgresql"))
        }

        if (extension.useTestcontainers.get()) {
            add("testImplementation", platform(libs.libraryOrThrow("testcontainers-bom")))
            add("testImplementation", libs.libraryOrThrow("testcontainers-postgresql"))
        }
    }
}
