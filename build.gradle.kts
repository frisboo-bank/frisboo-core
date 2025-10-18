import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

group = "com.frisboo.corebanking"
version = "1.0.0"
description = "Core files for core banking services"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
	compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
