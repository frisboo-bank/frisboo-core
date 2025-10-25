package com.frisboo.corebanking.conventionplugin.extensions.boms

import com.frisboo.corebanking.conventionplugin.Constants.Configurations
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public typealias ExposedBomExtension = BomExtensionSpec
public typealias JacksonBomExtension = BomExtensionSpec
public typealias JunitBomExtension = BomExtensionSpec
public typealias KotlinBomExtension = BomExtensionSpec
public typealias KotlinxCoroutinesBomExtension = BomExtensionSpec
public typealias SpringBootBomExtension = BomExtensionSpec
public typealias SpringdocOpenapiBomExtension = BomExtensionSpec
public typealias TestcontainersBomExtension = BomExtensionSpec

public open class BomExtension
@Inject constructor(objects: ObjectFactory, libs: VersionCatalog) {
    public val customBoms: NamedDomainObjectContainer<CustomBomExtensionSpec> =
        objects.domainObjectContainer(CustomBomExtensionSpec::class.java)
    public val exposed: BomExtensionSpec =
        objects.newInstance(BomExtensionSpec::class.java, libs, "exposed-bom", Configurations.Bom.EXPOSED, false)
    public val jackson: BomExtensionSpec =
        objects.newInstance(BomExtensionSpec::class.java, libs, "jackson-bom", Configurations.Bom.JACKSON, false)
    public val junit: BomExtensionSpec =
        objects.newInstance(BomExtensionSpec::class.java, libs, "junit-bom", Configurations.Bom.JUNIT, true)
    public val kotlin: BomExtensionSpec =
        objects.newInstance(BomExtensionSpec::class.java, libs, "kotlin-bom", Configurations.Bom.KOTLIN, false)
    public val kotlinxCoroutines: BomExtensionSpec =
        objects.newInstance(
            BomExtensionSpec::class.java,
            libs,
            "kotlinx-coroutines-bom",
            Configurations.Bom.KOTLINX_COROUTINES,
            false,
        )
    public val springBoot: BomExtensionSpec =
        objects.newInstance(
            BomExtensionSpec::class.java,
            libs,
            "spring-boot-bom",
            Configurations.Bom.SPRING_BOOT,
            false,
        )
    public val springdocOpenapi: BomExtensionSpec =
        objects.newInstance(
            BomExtensionSpec::class.java,
            libs,
            "springdoc-openapi-bom",
            Configurations.Bom.SPRINGDOC_OPENAPI,
            false,
        )
    public val testcontainers: BomExtensionSpec =
        objects.newInstance(
            BomExtensionSpec::class.java,
            libs,
            "testcontainers-bom",
            Configurations.Bom.TESTCONTAINERS,
            true,
        )

    public fun customBoms(action: Action<NamedDomainObjectContainer<CustomBomExtensionSpec>>) {
        action.execute(customBoms)
    }

    public fun exposed(action: Action<BomExtensionSpec>) {
        action.execute(exposed)
    }

    public fun jackson(action: Action<BomExtensionSpec>) {
        action.execute(jackson)
    }

    public fun junit(action: Action<BomExtensionSpec>) {
        action.execute(junit)
    }

    public fun kotlin(action: Action<BomExtensionSpec>) {
        action.execute(kotlin)
    }

    public fun kotlinxCoroutines(action: Action<BomExtensionSpec>) {
        action.execute(kotlinxCoroutines)
    }

    public fun springBoot(action: Action<BomExtensionSpec>) {
        action.execute(springBoot)
    }

    public fun springdocOpenapi(action: Action<BomExtensionSpec>) {
        action.execute(springdocOpenapi)
    }

    public fun testcontainers(action: Action<BomExtensionSpec>) {
        action.execute(testcontainers)
    }
}
