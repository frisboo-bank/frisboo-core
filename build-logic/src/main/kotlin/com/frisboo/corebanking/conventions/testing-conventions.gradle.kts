package com.frisboo.corebanking.conventions

plugins {
    id("org.jetbrains.kotlinx.kover")
    id("org.gradle.test-retry")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

dependencies {
}
