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
    val jvmTargetVersion = libs.versions.jvmTarget
    toolchain {
        languageVersion.set(jvmTargetVersion.map(JavaLanguageVersion::of))
    }
}

kotlin {
    val kotlinVersion = libs.versions.kotlinLanguage
    val jvmTargetVersion = libs.versions.jvmTarget

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

gradlePlugin {
    plugins {
        register("kotlinConvention") {
            id = libs.plugins.frisboo.convention.kotlin.get().pluginId
            implementationClass = "KotlinConventionModulePlugin"
        }

        register("MessagingConvention") {
            id = libs.plugins.frisboo.convention.messaging.get().pluginId
            implementationClass = "MessagingConventionModulePlugin"
        }

        register("OpenapiConvention") {
            id = libs.plugins.frisboo.convention.openapi.get().pluginId
            implementationClass = "OpenapiConventionModulePlugin"
        }

        register("PersistenceConvention") {
            id = libs.plugins.frisboo.convention.persistence.get().pluginId
            implementationClass = "PersistenceConventionModulePlugin"
        }

        register("QualityConvention") {
            id = libs.plugins.frisboo.convention.quality.get().pluginId
            implementationClass = "QualityConventionModulePlugin"
        }

        register("SpringBootConvention") {
            id = libs.plugins.frisboo.convention.spring.boot.get().pluginId
            implementationClass = "SpringBootConventionModulePlugin"
        }
    }
}

dependencies {
    implementation(plugin(libs.plugins.dependency.analysis))
    implementation(plugin(libs.plugins.detekt))
//    implementation(plugin(libs.plugins.dokka))
    implementation(plugin(libs.plugins.gradle.versions))
    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.kotlin.spring))
//    implementation(plugin(libs.plugins.kover))
    implementation(plugin(libs.plugins.openapi.generator))
//    implementation(plugin(libs.plugins.protobuf))
    implementation(plugin(libs.plugins.spotless))
    implementation(plugin(libs.plugins.spring.boot))
    implementation(plugin(libs.plugins.springdoc.openapi))
//    implementation(plugin(libs.plugins.test.retry))
    compileOnly(libs.kotlin.gradle.plugin)
}

// Helper function that transforms a Gradle Plugin alias from a
// Version Catalog into a valid dependency notation for buildSrc
// See https://docs.gradle.org/current/userguide/version_catalogs.html#sec:buildsrc-version-catalog
fun plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
