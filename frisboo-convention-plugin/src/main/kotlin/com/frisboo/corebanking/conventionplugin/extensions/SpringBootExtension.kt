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
package com.frisboo.corebanking.conventionplugin.extensions

import com.frisboo.corebanking.conventionplugin.ConfigurationConstants
import com.frisboo.corebanking.conventionplugin.utils.gradleProperty
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class SpringBootExtension
@Inject
constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    libs: VersionCatalog
) {
    public val enabled: Property<Boolean> =
        objects.property<Boolean>().convention(
            providers.gradleProperty(ConfigurationConstants.SpringBoot.ENABLE_SPRING, String::toBoolean).orElse(true),
        )

    public fun enabled(value: Boolean): Unit = enabled.set(value)
}
