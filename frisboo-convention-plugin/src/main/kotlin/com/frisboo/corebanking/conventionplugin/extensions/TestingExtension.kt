package com.frisboo.corebanking.conventionplugin.extensions

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import javax.inject.Inject

public open class TestingExtension
@Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    layout: ProjectLayout,
    libs: VersionCatalog,
) {

    public val testJvmArguments: ListProperty<String> = objects.listProperty<String>().convention(
//                gradleProperty(providers, Configurations.Plugin.TEST_JVM_ARGS) {
//                    it.split(",").map(String::trim)
//                }.orElse(
            listOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
            ),
//                ),
        )

}
