package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.SpringBootExtension
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import org.gradle.api.Project

public class SpringBootManager(
    private val project: Project,
    private val ext: SpringBootExtension,
) {
    private val libs = project.getLibs()

    public fun configure() {
        if (!ext.enabled.get()) return

        project.pluginManager.apply("org.springframework.boot")

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.pluginManager.apply("org.jetbrains.kotlin.plugin.spring")
        }

//        extensions.configure<SpringBootExtension> {
//            buildInfo()
//        }
    }
}

