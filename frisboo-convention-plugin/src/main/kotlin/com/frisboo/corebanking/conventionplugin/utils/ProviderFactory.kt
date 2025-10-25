package com.frisboo.corebanking.conventionplugin.utils

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal fun <T : Any> ProviderFactory.gradleProperty(key: String, converter: (String) -> T): Provider<T> =
    gradleProperty(key).map(converter)
