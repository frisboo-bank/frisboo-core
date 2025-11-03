plugins {
    id("kotlin-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")

        @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class) abiValidation {
            enabled.set(true)
        }
    }
}

dependencies {
    api(platform(libs.exposed.bom))
    api(project(":frisboo-core"))
    api(libs.kotlin.logging)
    api(libs.exposed.spring.boot.starter)
    api(libs.exposed.kotlin.datetime)
    api(libs.exposed.r2dbc)
}
