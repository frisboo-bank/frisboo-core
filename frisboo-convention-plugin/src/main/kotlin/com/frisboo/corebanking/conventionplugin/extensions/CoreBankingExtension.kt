package com.frisboo.corebanking.conventionplugin.extensions

import com.frisboo.corebanking.conventionplugin.ConfigurationConstants
import com.frisboo.corebanking.conventionplugin.utils.gradleProperty
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class CoreBankingExtension
@Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    libs: VersionCatalog,
) {
    public val enabled: Property<Boolean> = objects.property<Boolean>().convention(
        providers.gradleProperty(ConfigurationConstants.CoreBanking.ENABLE_COREBANKING, String::toBoolean).orElse(true),
    )

    public fun enabled(value: Boolean): Unit = enabled.set(value)
}
