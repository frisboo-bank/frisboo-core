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
                from(files("../version-catalog/libs.versions.toml"))
            },
        )
    }
}

rootProject.name = "build-logic"
