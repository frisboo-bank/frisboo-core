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
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `jvm-test-suite`
    id("kotlin-conventions")
    alias(libs.plugins.plugin.publish)
}

description = "Gradle plugin that provides conventions for core banking apis"

gradlePlugin {
    plugins {
        create("frisbooConvention") {
            id = "com.frisboo.corebanking.frisboo-convention-plugin"
            displayName = "Frisboo Core Banking Convention Plugin"
            description = "Gradle plugin that provides conventions for core banking apis"
            tags = listOf(
                "frisboo",
                "frisboo-core-banking",
                "convention-plugin",
            )
            implementationClass = "com.frisboo.corebanking.conventionplugin.FrisbooCoreBankingConventionPlugin"
        }
    }
}

dependencies {
    implementation(libs.restrict.imports.plugin)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {

            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation(gradleTestKit())
                implementation(libs.kotest.assertions.core)
            }

            targets.configureEach {
                testTask.configure {

                    classpath += files(tasks.named("pluginUnderTestMetadata"))

                    shouldRunAfter(tasks.named("test"))

                    testLogging {
                        events("passed", "skipped", "failed")
                        exceptionFormat = TestExceptionFormat.FULL
                    }

                    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                }
            }
        }
    }
}

tasks.register("publishToLocal") {
    group = "Publishing"
    description = "Publishes the project to the local Maven repository"
    dependsOn("publishToMavenLocal")
}

tasks.withType<ValidatePlugins>().configureEach {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}
