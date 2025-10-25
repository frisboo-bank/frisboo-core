package com.frisboo.corebanking.conventionplugin.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

internal fun Project.getLibs(): VersionCatalog =
    extensions.findByType(VersionCatalogsExtension::class.java)?.named("libs")
        ?: error("Version catalog `libs` not found")
