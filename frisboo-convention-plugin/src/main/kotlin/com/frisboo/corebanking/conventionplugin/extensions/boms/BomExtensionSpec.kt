package com.frisboo.corebanking.conventionplugin.extensions.boms

import com.frisboo.corebanking.conventionplugin.utils.gradleProperty
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Extension specification used to configure a BOM.
 *
 * @param alias The Version Catalog alias for the BOM (e.g., "jackson-bom").
 * @param configurationKey The Gradle property key to toggle this BOM (e.g., Constants.Configurations.Bom.JACKSON).
 * @param defaultTestOnly Whether this BOM should default to test-only usage.
 */
public open class BomExtensionSpec
@Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    libs: VersionCatalog,
    alias: String,
    configurationKey: String,
    defaultTestOnly: Boolean,
) {
    public val enabled: Property<Boolean> = objects.property<Boolean>().convention(
        providers.gradleProperty(configurationKey, String::toBoolean).orElse(true),
    )
    internal val coordinates: Provider<MinimalExternalModuleDependency> = libs.libraryOrThrow(alias)
    public val testOnly: Property<Boolean> = objects.property<Boolean>().convention(defaultTestOnly)

    public fun enabled(value: Boolean): Unit = enabled.set(value)
    public fun testOnly(value: Boolean): Unit = testOnly.set(value)
}
