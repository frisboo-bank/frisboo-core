pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    includeBuild("build-logic")
}

plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    versionCatalogs {
        create(
            "versionLibs",
            Action {
                from(files("version-catalog/libs.versions.toml"))
            },
        )
    }
}

rootProject.name = "core"

include("version-catalog")
