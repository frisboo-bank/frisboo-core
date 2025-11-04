/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

internal fun VersionCatalog.getVersionOrFail(alias: String): String =
    findVersion(alias)
        .orElseThrow {
            IllegalStateException("Missing version `$alias` from libs.versions.toml")
        }.requiredVersion

internal fun VersionCatalog.optionalVersion(alias: String): String? =
    findVersion(alias).map { it.requiredVersion }.orElse(null)

internal fun VersionCatalog.libraryOrThrow(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Missing library `$alias` in libs.versions.toml, available libraries: ${toString()}")
    }

internal fun VersionCatalog.pluginOrThrow(alias: String): Provider<PluginDependency> =
    findPlugin(alias).orElseThrow {
        IllegalStateException("Missing plugin `$alias` in libs.versions.toml, available plugins: ${toString()}")
    }

internal fun VersionCatalog.pluginIdOrThrow(alias: String): String = pluginOrThrow(alias).get().pluginId
