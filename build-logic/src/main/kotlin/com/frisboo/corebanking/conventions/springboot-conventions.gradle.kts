package com.frisboo.corebanking.conventions

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.spring")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

springBoot {
    buildInfo()
}

dependencies {
    implementation(libs.findLibrary("spring-boot-starter-web").get())
    implementation(libs.findLibrary("spring-boot-starter-validation").get())
    implementation(libs.findLibrary("spring-boot-starter-actuator").get())
    implementation(platform(libs.findLibrary("spring-boot-bom").get()))
}

tasks.bootJar {
    archiveFileName.set("${project.name}-${project.version}.jar")
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Created-By" to "Gradle ${gradle.gradleVersion}",
        )
    }
}
