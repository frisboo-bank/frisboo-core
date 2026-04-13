/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
rootProject.name = "core"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("frisboo-bom")
include("frisboo-core")
include("frisboo-tests")

include("frisboo-auth")
include("frisboo-config")
include("frisboo-coordination")
include("frisboo-crypto")
include("frisboo-data")
include("frisboo-grpc")
include("frisboo-http")
include("frisboo-messaging")
include("frisboo-observability")
include("frisboo-persistence")
include("frisboo-quota")
include("frisboo-statemanager")
include("frisboo-resilience")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
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

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
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
            from("com.frisboo.corebanking:version-catalog:0.0.1-alpha.1-SNAPSHOT")
        }
    }
}
