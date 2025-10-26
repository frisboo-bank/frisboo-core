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

import com.frisboo.corebanking.conventionplugin.extensions.boms.BomExtension
import com.frisboo.corebanking.conventionplugin.utils.getVersionOrFail
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class PluginExtension
@Inject constructor(
    private val objects: ObjectFactory,
    providers: ProviderFactory,
    layout: ProjectLayout,
    libs: VersionCatalog,
) {
    private val projectName = layout.projectDirectory.asFile.name

    public val frisbooCoreBankingVersion: Property<String> = objects.property<String>().convention(
        providers.gradleProperty("frisboo-corebanking-version")
            .orElse(libs.getVersionOrFail("frisboo-corebanking-version")),
    )

    public val artifactId: Property<String> = objects.property<String>().convention(
        projectName.removePrefix("frisboo-corebanking-").removeSuffix("-plugin"),
    )

    public val displayName: Property<String> = objects.property<String>().convention(
        artifactId.map { id ->
            id.split("-").joinToString(" ") { part -> part.replaceFirstChar { it.titlecase() } }
        },
    )

    public fun frisbooCoreBankingVersion(value: String): Unit = frisbooCoreBankingVersion.set(value)

    public fun artifactId(value: String): Unit = artifactId.set(value)

    public fun displayName(value: String): Unit = displayName.set(value)

    public val bom: BomExtension = objects.newInstance<BomExtension>(libs)
    public val coreBanking: CoreBankingExtension = objects.newInstance<CoreBankingExtension>(libs)
    public val grpc: GRPCExtension = objects.newInstance<GRPCExtension>(libs)
    public val kotlin: KotlinExtention = objects.newInstance<KotlinExtention>(libs)
    public val messaging: MessagingExtension = objects.newInstance<MessagingExtension>(libs)
    public val openApi: OpenapiExtension = objects.newInstance<OpenapiExtension>(libs)
    public val persistence: PersistenceExtension = objects.newInstance<PersistenceExtension>(libs)
    public val quality: QualityExtension = objects.newInstance<QualityExtension>()
    public val springBoot: SpringBootExtension = objects.newInstance<SpringBootExtension>(libs)
    public val telemetry: TelemetryExtension = objects.newInstance<TelemetryExtension>(libs)
    public val testing: TestingExtension = objects.newInstance<TestingExtension>(libs)
}
