package com.frisboo.corebanking.conventionplugin.extensions.boms

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

public open class BomExtension
@Inject constructor(objects: ObjectFactory, libs: VersionCatalog) {
    public val customBoms: NamedDomainObjectContainer<CustomBomExtension> =
        objects.domainObjectContainer(CustomBomExtension::class.java)
    public val exposed: ExposedBomExtension = objects.newInstance(libs)
    public val jackson: JacksonBomExtension = objects.newInstance(libs)
    public val junit: JunitBomExtension = objects.newInstance(libs)
    public val kotlin: KotlinBomExtension = objects.newInstance(libs)
    public val kotlinxCoroutines: KotlinxCoroutinesBomExtension = objects.newInstance(libs)
    public val springBoot: SpringBootBomExtension = objects.newInstance(libs)
    public val springdocOpenapi: SpringdocOpenapiBomExtension = objects.newInstance(libs)
    public val testcontainers: TestcontainersBomExtension = objects.newInstance(libs)

    public fun customBoms(action: Action<NamedDomainObjectContainer<CustomBomExtension>>) {
        action.execute(customBoms)
    }

    public fun exposed(action: Action<ExposedBomExtension>) {
        action.execute(exposed)
    }

    public fun jackson(action: Action<JacksonBomExtension>) {
        action.execute(jackson)
    }

    public fun junit(action: Action<JunitBomExtension>) {
        action.execute(junit)
    }

    public fun kotlin(action: Action<KotlinBomExtension>) {
        action.execute(kotlin)
    }

    public fun kotlinxCoroutines(action: Action<KotlinxCoroutinesBomExtension>) {
        action.execute(kotlinxCoroutines)
    }

    public fun springBoot(action: Action<SpringBootBomExtension>) {
        action.execute(springBoot)
    }

    public fun springdocOpenapi(action: Action<SpringdocOpenapiBomExtension>) {
        action.execute(springdocOpenapi)
    }

    public fun testcontainers(action: Action<TestcontainersBomExtension>) {
        action.execute(testcontainers)
    }
}
