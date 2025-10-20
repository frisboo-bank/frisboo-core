package com.frisboo.corebanking.conventions

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

kotlin {
    val kotlinVersion = libs.findVersion("kotlinLanguage").get().requiredVersion
    val jvmTargetVersion = libs.findVersion("jvmTarget").get().requiredVersion

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion))
    }

    explicitApi()

    compilerOptions {
        apiVersion.set(KotlinVersion.Companion.fromVersion(kotlinVersion))
        languageVersion.set(KotlinVersion.Companion.fromVersion(kotlinVersion))
        jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))

        allWarningsAsErrors.set(true)
        progressiveMode.set(true)
        optIn.add("kotlin.RequiresOptIn")
        jvmDefault.set(JvmDefaultMode.ENABLE)
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict"))
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.findLibrary("kotlin-bom").get())
    implementation(libs.findLibrary("kotlin-gradle-plugin").get())
    implementation(libs.findLibrary("kotlinx-coroutines-bom").get())
    implementation(libs.findLibrary("kotlinx-coroutines-core"))
    compileOnly(libs.findLibrary("jetbrains-annotations").get())

}
