package com.frisboo.corebanking.conventionplugin.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public class QualityExtension
@Inject
constructor(objects: ObjectFactory, provider: ProviderFactory) {
    public val enabled: Property<Boolean> =
        objects.property<Boolean>().convention(
            provider
                .gradleProperty("quality.dokka.enabled")
                .map { it.toBoolean() }
                .orElse(true),
        )

}
