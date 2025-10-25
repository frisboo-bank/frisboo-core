rootProject.name = "core"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
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
            "libs",
            Action {
                from(files("frisboo-versions/libs.versions.toml"))
            },
        )
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.3"
}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

include("frisboo-bom")
include("frisboo-convention-plugin")
//include("frisboo-core")
//include("frisboo-spring-boot")
include("frisboo-versions")
