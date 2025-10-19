// ==============================================================================
// Plugin
// ==============================================================================

plugins {
    // ==============================================================================
    // Core Framework Plugins
    // ==============================================================================
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)

    // ==============================================================================
    // Development & Testing Plugins
    // ==============================================================================
    alias(libs.plugins.test.retry)
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.kover)

    // ==============================================================================
    // Analysis/quality
    // ==============================================================================
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

group = "com.frisboo.corebanking"
version = "1.0.0"
description = "Core files for core banking services"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
	compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

spotless {
    kotlin {
        ktlint()
        target("**/*.kt")
    }
    kotlinGradle {
        ktlint()
        target("**/*.kts")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

tasks.withType<Test> {
	useJUnitPlatform()
}
