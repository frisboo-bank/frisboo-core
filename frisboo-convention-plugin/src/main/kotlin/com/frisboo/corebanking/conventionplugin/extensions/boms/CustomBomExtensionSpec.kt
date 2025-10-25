package com.frisboo.corebanking.conventionplugin.extensions.boms

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class CustomBomExtensionSpec
@Inject constructor(
    public val name: String,
    objects: ObjectFactory,
) {
    public val coordinates: Property<String> = objects.property<String>()
    public val version: Property<String> = objects.property<String>()
    public val testOnly: Property<Boolean> = objects.property<Boolean>().convention(false)

    public fun coordinates(value: String): Unit = coordinates.set(value)
    public fun version(value: String): Unit = version.set(value)
    public fun testOnly(value: Boolean): Unit = testOnly.set(value)
}
