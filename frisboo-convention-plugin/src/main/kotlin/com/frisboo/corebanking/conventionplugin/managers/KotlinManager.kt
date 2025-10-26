package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.DependencyConstants
import com.frisboo.corebanking.conventionplugin.extensions.KotlinExtention
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

public class KotlinManager(
    private val project: Project,
    private val ext: KotlinExtention,
) {
    private val libs = project.getLibs()

    public fun configure() {
        project.dependencies {
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.REACTOR_KOTLIN_EXTENSIONS))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.KOTLIN_REFLECT))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.KOTLINX_COROUTINES_CORE))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.KOTLINX_COROUTINES_REACTOR))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.ARROW_KT_CORE))
            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.ARROW_KT_COROUTINES))

            add("compileOnly", libs.libraryOrThrow(DependencyConstants.Libraries.JETBRAINS_ANNOTATIONS))

            // Test
            add("testImplementation", libs.libraryOrThrow(DependencyConstants.Libraries.REACTOR_TEST))
            add("testImplementation", libs.libraryOrThrow(DependencyConstants.Libraries.KOTLIN_TEST_JUNIT5))
            add("testImplementation", libs.libraryOrThrow(DependencyConstants.Libraries.KOTLINX_COROUTINES_TEST))
            add("testRuntimeOnly", libs.libraryOrThrow(DependencyConstants.Libraries.JUNIT_PLATFORM_LAUNCHER))
        }
    }
}
