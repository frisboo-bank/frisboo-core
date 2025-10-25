package com.frisboo.corebanking.conventionplugin

import com.frisboo.corebanking.conventionplugin.extensions.boms.BomExtension
import org.gradle.api.Action
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class FrisbooCoreBankingConventionExtension
@Inject
constructor(
    private val objects: ObjectFactory,
    providers: ProviderFactory,
    layout: ProjectLayout,
    libs: VersionCatalog,
) {
    private val projectName = layout.projectDirectory.asFile.name

    public val frisbooCoreBankingVersion: Property<String> =
        objects.property<String>().convention(
            providers.gradleProperty("frisboo-corebanking")
//                .orElse(libs.getVersionOrFail("frisboo-corebanking-core")),
        )

    public val artifactId: Property<String> =
        objects.property<String>().convention(
            projectName.removePrefix("frisboo-corebanking-").removeSuffix("-plugin"),
        )

    public val displayName: Property<String> =
        objects.property<String>().convention(
            artifactId.map { id ->
                id.split("-").joinToString(" ") { part -> part.replaceFirstChar { it.titlecase() } }
            },
        )

    public fun frisbooCoreBankingVersion(value: String): Unit = frisbooCoreBankingVersion.set(value)
    public fun artifactId(value: String): Unit = artifactId.set(value)
    public fun displayName(value: String): Unit = displayName.set(value)

    public val bom: BomExtension = objects.newInstance<BomExtension>(libs)
//    public val grpc: GRPCExtension = objects.newInstance<GRPCExtension>(libs)
//    public val messaging: MessagingExtension = objects.newInstance<MessagingExtension>(libs)
//    public val openApi: OpenapiExtension = objects.newInstance<OpenapiExtension>(libs)
//    public val persistence: PersistenceExtension = objects.newInstance<PersistenceExtension>(libs)
//    public val quality: QualityExtension = objects.newInstance<QualityExtension>()
//    public val springBoot: SpringBootExtension = objects.newInstance<SpringBootExtension>(libs)
//    public val telemetry: TelemetryExtension = objects.newInstance<TelemetryExtension>(libs)

    public fun bom(action: Action<BomExtension>) {
        action.execute(bom)
    }

//    public fun grpc(action: Action<GRPCExtension>) {
//        action.execute(grpc)
//    }
//
//    public fun messaging(action: Action<MessagingExtension>) {
//        action.execute(messaging)
//    }
//
//    public fun openApi(action: Action<OpenapiExtension>) {
//        action.execute(openApi)
//    }
//
//    public fun persistence(action: Action<PersistenceExtension>) {
//        action.execute(persistence)
//    }
//
//    public fun quality(action: Action<QualityExtension>) {
//        action.execute(quality)
//    }
//
//    public fun springBoot(action: Action<SpringBootExtension>) {
//        action.execute(springBoot)
//    }
//
//    public fun telemetry(action: Action<TelemetryExtension>) {
//        action.execute(telemetry)
//    }
}

