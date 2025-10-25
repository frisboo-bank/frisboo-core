package com.frisboo.corebanking.conventionplugin.managers.language

import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.getVersionOrFail
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public class KotlinManager(private val project: Project) {
    private val libs = project.getLibs()
    private val kotlinVersion = libs.getVersionOrFail("kotlinLanguage")
    private val jvmTargetVersion = libs.getVersionOrFail("jvmTarget")

    public fun configure() {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.configure<KotlinJvmProjectExtension> {
                jvmToolchain(jvmTargetVersion.toInt())
            }

            project.tasks.withType<KotlinCompile>().configureEach { t ->
                t.compilerOptions {
                    apiVersion.set(KotlinVersion.Companion.fromVersion(kotlinVersion))
                    languageVersion.set(KotlinVersion.Companion.fromVersion(kotlinVersion))
                    jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))
                    allWarningsAsErrors.set(true)
                    optIn.add("kotlin.RequiresOptIn")
                    freeCompilerArgs.addAll(listOf("-Xjsr305=strict"))
                }
            }

            project.dependencies {
                add("implementation", platform(libs.libraryOrThrow("kotlin-bom")))
                add("compileOnly", libs.libraryOrThrow("jetbrains-annotations"))
            }
        }
    }
}
