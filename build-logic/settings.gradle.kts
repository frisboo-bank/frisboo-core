rootProject.name = "build-logic"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


@Suppress("UnstableApiUsage") dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "FrisbooGitHubPackages"
            url = uri("https://maven.pkg.github.com/jolafrite/frisboo-core-banking")
            credentials {
                username = providers.gradleProperty("frisboo.gpr.user").orNull ?: System.getenv("FRISBOO_GPR_USERNAME")
                password = providers.gradleProperty("frisboo.gpr.key").orNull ?: System.getenv("FRISBOO_GPR_TOKEN")
            }
        }
        mavenLocal()
    }

    versionCatalogs {
        create("baseLibs") {
            from("com.frisboo.corebanking:version-catalog:0.0.1-alpha1")
        }
    }
}
