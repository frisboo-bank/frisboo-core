package com.frisboo.corebanking.conventionplugin

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

internal fun VersionCatalog.getVersionOrFail(alias: String): String = findVersion(alias).orElseThrow {
    IllegalStateException("Missing version `$alias` from libs.versions.toml")
}.requiredVersion

internal fun VersionCatalog.optionalVersion(alias: String): String? = findVersion(alias).map {
    it.requiredVersion
}.orElse(null)

internal fun VersionCatalog.libraryOrThrow(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Missing library `$alias` in libs.versions.toml")
    }
