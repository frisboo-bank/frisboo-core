rootProject.name = "build-logic"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage") dependencyResolutionManagement {
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
                from("com.frisboo.corebanking:version-catalog:0.0.1")
            },
        )
    }
}
