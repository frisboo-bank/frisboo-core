package com.frisboo.corebanking.conventions

plugins {
    id("org.jetbrains.dokka")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

dependencies {
}
