package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.PluginExtension
import de.skuzzle.restrictimports.gradle.RestrictImportsExtension
import de.skuzzle.restrictimports.gradle.RestrictImportsPlugin
import org.gradle.api.Project

public class RestrictImportsManager(private val project: Project, private val ext: PluginExtension) {
    public fun configure() {
        project.pluginManager.apply(RestrictImportsPlugin::class.java)

        project.extensions.configure(RestrictImportsExtension::class.java) { restrictImports ->
            restrictImports.reason.set("Please use JUnit 5 (JUnit Jupiter) instead of JUnit 4")
            restrictImports.bannedImports.set(listOf("org.junit.**"))
            restrictImports.allowedImports.set(listOf("org.junit.jupiter.**"))
        }

        project.tasks.named("check").configure {
            it.dependsOn("restrictImports")
        }
    }
}
