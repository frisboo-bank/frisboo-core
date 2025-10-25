package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.boms.BomExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies

public class BomManager(
    private val project: Project,
    private val ext: BomExtension,
) {
    public fun configure() {
        project.dependencies {
            configurePredefined()
            configureCustom()
        }
    }

    private fun DependencyHandler.configurePredefined() {
        listOf(
            Triple(ext.exposed.enabled, ext.exposed.coordinates, ext.exposed.testOnly),
            Triple(ext.jackson.enabled, ext.jackson.coordinates, ext.jackson.testOnly),
            Triple(ext.junit.enabled, ext.junit.coordinates, ext.junit.testOnly),
            Triple(ext.kotlin.enabled, ext.kotlin.coordinates, ext.kotlin.testOnly),
            Triple(ext.kotlinxCoroutines.enabled, ext.kotlinxCoroutines.coordinates, ext.kotlinxCoroutines.testOnly),
            Triple(ext.springBoot.enabled, ext.springBoot.coordinates, ext.springBoot.testOnly),
            Triple(ext.springdocOpenapi.enabled, ext.springdocOpenapi.coordinates, ext.springdocOpenapi.testOnly),
            Triple(ext.testcontainers.enabled, ext.testcontainers.coordinates, ext.testcontainers.testOnly),
        ).forEach { (enabled, coordinates, testOnly) -> applyIfEnabled(enabled, coordinates, testOnly) }
    }

    private fun DependencyHandler.configureCustom() {
        ext.customBoms.configureEach { bom ->
            val coordinates = bom.coordinates.orNull
            val version = bom.version.orNull

            require(!coordinates.isNullOrBlank()) { "Missing coordinates for BOM '${bom.name}'." }

            require(!version.isNullOrBlank()) { "Missing version for BOM '${bom.name}'." }

            val dep = platform("$coordinates:$version")

            if (!bom.testOnly.getOrElse(false)) {
                add("implementation", dep)
            }

            add("testImplementation", dep)
        }
    }

    private fun DependencyHandler.applyIfEnabled(
        enabled: Property<Boolean>,
        coordinates: Provider<MinimalExternalModuleDependency>,
        testOnly: Property<Boolean>,
    ) {
        if (!enabled.getOrElse(true)) return

        val dep = platform(coordinates.get())

        if (!testOnly.getOrElse(false)) {
            add("implementation", dep)
        }
        add("testImplementation", dep)
    }
}
