package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.DependencyConstants
import com.frisboo.corebanking.conventionplugin.extensions.SpringBootExtension
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import com.frisboo.corebanking.conventionplugin.utils.pluginIdOrThrow
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.dsl.SpringBootExtension as SpringBootDslExtension

public class SpringBootManager(
    private val project: Project,
    private val ext: SpringBootExtension,
) {
    private val libs = project.getLibs()

    public fun configure() {
        if (!ext.enabled.get()) return

        project.pluginManager.apply(libs.pluginIdOrThrow(DependencyConstants.Plugins.SPRING_BOOT))
        project.pluginManager.apply(libs.pluginIdOrThrow(DependencyConstants.Plugins.SPRING_DEPENDENCY_MANAGEMENT))

        project.plugins.withId(libs.pluginIdOrThrow(DependencyConstants.Plugins.KOTLIN_JVM)) {
            project.pluginManager.apply(libs.pluginIdOrThrow(DependencyConstants.Plugins.KOTLIN_SPRING))
        }

        project.dependencies {
            add("implementation", "org.springframework.boot:spring-boot-starter-hateoas")
            add("implementation", "org.springframework.boot:spring-boot-starter-security")
            add("implementation", libs.libraryOrThrow("jackson-module-kotlin"))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.SPRING_BOOT_STARTER_ACTUATOR))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.SPRING_BOOT_STARTER_VALIDATION))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.SPRING_BOOT_STARTER_WEBFLUX))
            add("developmentOnly", "org.springframework.boot:spring-boot-devtools")
            add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")
            add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
            add("testImplementation", "org.springframework.security:spring-security-test")
        }

        project.extensions.configure<SpringBootDslExtension> {
            buildInfo()
        }

        project.tasks.named<BootJar>("bootJar") {
            archiveFileName.set("${project.name}-${project.version}.jar")

            manifest.attributes(
                mapOf(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version.toString(),
                    "Created-By" to "Gradle ${project.gradle.gradleVersion}",
                ),
            )
        }
    }
}

