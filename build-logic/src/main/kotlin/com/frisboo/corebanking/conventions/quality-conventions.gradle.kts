package com.frisboo.corebanking.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.autonomousapps.dependency-analysis")
    id("com.diffplug.spotless")
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

private val headerFile = rootProject.layout.projectDirectory.file("config/license-header.txt")
private val editorConfig = rootProject.layout.projectDirectory.file(".editorconfig")
private val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
private val delimiter =
    "^\\s*(plugins|pluginManagement|import|buildscript|" +
            "dependencyResolutionManagement|enableFeaturePreview|include|rootProject)\\b"

configure<SpotlessExtension> {
    val commonExcludes =
        listOf(
            "**/build/**",
            "**/build-*/**",
            "**/.gradle/**",
            "**/.idea/**",
            "**/.git/**",
            "**/generated/**",
            "**/.gradle-test-kit/**",
        )

    kotlin {
        target("**/*.kt")
        targetExclude(commonExcludes)
        ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(headerFile)
        lineEndings = LineEnding.UNIX
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude(commonExcludes)
        ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(headerFile, delimiter)
        lineEndings = LineEnding.UNIX
    }

    format("misc") {
        target(
            "**/*.md",
            "**/*.properties",
            "**/*.yml",
            "**/*.yaml",
            "**/*.xml",
            "**/.gitignore",
            "**/*.txt",
        )
        targetExclude(commonExcludes)
        trimTrailingWhitespace()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

private val detektConfig = rootProject.layout.projectDirectory.file("config/detekt/detekt.yml")
private val detektBaseline = rootProject.layout.projectDirectory.file("config/detekt/detekt-baseline.xml").asFile

configure<DetektExtension> {
    toolVersion = libs.findVersion("detekt").get().requiredVersion
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(detektConfig)
    baseline = detektBaseline
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JvmTarget.JVM_21.target
    autoCorrect = true

    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
        md.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck", "detekt")
}
