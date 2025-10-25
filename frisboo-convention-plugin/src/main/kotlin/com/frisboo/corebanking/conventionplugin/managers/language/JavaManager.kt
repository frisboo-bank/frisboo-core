package com.frisboo.corebanking.conventionplugin.managers.language

import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.getVersionOrFail
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

public class JavaManager(private val project: Project) {
    private val libs = project.getLibs()
    private val jvmTargetVersion = libs.getVersionOrFail("jvmTarget")

    public fun configure() {
        project.pluginManager.apply("java")

        project.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion))
            withSourcesJar()
            withJavadocJar()
        }

        project.tasks.withType<JavaCompile>().configureEach {
            it.options.encoding = "UTF-8"
            it.options.release.set(jvmTargetVersion.toInt())
            it.options.compilerArgs.addAll(
                listOf("-parameters", "-Xlint:all,-serial"),
            )
        }

        project.tasks.withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }
}
