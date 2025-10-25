package com.frisboo.corebanking.conventionplugin.extensions.boms

import com.frisboo.corebanking.conventionplugin.Constants
import com.frisboo.corebanking.conventionplugin.gradleProperty
import com.frisboo.corebanking.conventionplugin.libraryOrThrow
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class JunitBomExtension
@Inject constructor(objects: ObjectFactory, providers: ProviderFactory, libs: VersionCatalog) {
    public val enabled: Property<Boolean> = objects.property<Boolean>().convention(
        providers.gradleProperty(Constants.Configurations.Bom.JUNIT, String::toBoolean).orElse(true),
    )
    internal val coordinates: Provider<MinimalExternalModuleDependency> = libs.libraryOrThrow("junit-bom")
    public val testOnly: Property<Boolean> = objects.property<Boolean>().convention(true)

    public fun enabled(value: Boolean): Unit = enabled.set(value)
    public fun testOnly(value: Boolean): Unit = testOnly.set(value)
}
