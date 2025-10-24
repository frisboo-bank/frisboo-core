package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.boms.BomExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies

public class BomManager(private val project: Project, private val ext: BomExtension) {
    public fun configure() {
        project.dependencies {
            configurePredefined()
            configureCustom()
        }
    }

    private fun DependencyHandler.configurePredefined() {
        listOf(
            Triple(ext.exposed.enabled, ext.exposed.coordinates, ext.exposed.testOnly),
        ).forEach { (enabled, coordinates, testOnly) ->
            applyIfEnabled(enabled, coordinates, testOnly)
        }
    }

    private fun DependencyHandler.configureCustom() {
        ext.customBoms.forEach { bom ->
            val coordinates = bom.coordinates.orNull
            val version = bom.version.orNull
            val testOnly = bom.testOnly

            require(!coordinates.isNullOrBlank()) { "Custom BOM '${bom.name}' must have 'coordinates' defined." }
            require(!version.isNullOrBlank()) { "Custom BOM '${bom.name}' must have 'version' defined." }

            val dependency = platform("$coordinates:$version")

            if (!testOnly.getOrElse(false)) {
                add("implementation", dependency)
            }
            add("testImplementation", dependency)
        }
    }

    private fun DependencyHandler.applyIfEnabled(
        enabled: Property<Boolean>,
        coordinates: Provider<MinimalExternalModuleDependency>,
        testOnly: Property<Boolean>,
    ) {
        if (!enabled.getOrElse(true)) return

        val dependency = platform(coordinates.get())

        if (!testOnly.getOrElse(false)) {
            add("implementation", dependency)
        }
        add("testImplementation", dependency)
    }
}
