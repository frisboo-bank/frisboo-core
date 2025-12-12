import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    base
    `java-gradle-plugin`
    `version-catalog`
    `kotlin-dsl`
}

java {
    val jvmTargetVersion = baseLibs.versions.jvm.target
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion.get()))
    }
}

kotlin {
    val kotlinVersion = baseLibs.versions.kotlin.language
    val jvmTargetVersion = baseLibs.versions.jvm.target

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
    implementation(plugin(baseLibs.plugins.kotlin.jvm))
    implementation(plugin(baseLibs.plugins.spotless))
    implementation(plugin(baseLibs.plugins.detekt))
}

fun plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

