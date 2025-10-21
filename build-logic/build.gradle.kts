import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "com.frisboo.corebanking"
version = "1.0.0"

java {
    val jvmTargetVersion = versionLibs.versions.jvmTarget
    toolchain {
        languageVersion.set(jvmTargetVersion.map(JavaLanguageVersion::of))
    }
}

kotlin {
    val kotlinVersion = versionLibs.versions.kotlinLanguage
    val jvmTargetVersion = versionLibs.versions.jvmTarget

    jvmToolchain {
        languageVersion.set(jvmTargetVersion.map(JavaLanguageVersion::of))
    }

    explicitApi()

    compilerOptions {
        apiVersion.set(kotlinVersion.map(KotlinVersion::fromVersion))
        languageVersion.set(kotlinVersion.map(KotlinVersion::fromVersion))
        jvmTarget.set(jvmTargetVersion.map(JvmTarget::fromTarget))

        allWarningsAsErrors.set(true)
        progressiveMode.set(true)
        optIn.add("kotlin.RequiresOptIn")
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    implementation(plugin(versionLibs.plugins.dependency.analysis))
    implementation(plugin(versionLibs.plugins.detekt))
    implementation(plugin(versionLibs.plugins.dokka))
    implementation(plugin(versionLibs.plugins.gradle.versions))
    implementation(plugin(versionLibs.plugins.kotlin.jvm))
    implementation(plugin(versionLibs.plugins.kotlin.spring))
    implementation(plugin(versionLibs.plugins.kover))
    implementation(plugin(versionLibs.plugins.openapi.generator))
    implementation(plugin(versionLibs.plugins.protobuf))
    implementation(plugin(versionLibs.plugins.spotless))
    implementation(plugin(versionLibs.plugins.spring.boot))
    implementation(plugin(versionLibs.plugins.springdoc.openapi))
    implementation(plugin(versionLibs.plugins.test.retry))
}

// Helper function that transforms a Gradle Plugin alias from a
// Version Catalog into a valid dependency notation for buildSrc
// See https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
fun plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

publishing {
    publications {
        create<MavenPublication>("buildLogic") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set("frisboo-core-banking-build-logic")
                description.set("Shared build logic for Frisboo core banking services")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
