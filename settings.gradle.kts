
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.3"
}

//@Suppress("UnstableApiUsage")
//toolchainManagement {
//    jvm {
//        javaRepositories {
//            repository("foojay") {
//                resolverClass.set(FoojayToolchainResolver::class.java)
//            }
//        }
//    }
//}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

include("frisboo-bom")
include("frisboo-convention-plugin")
include("frisboo-core")
include("frisboo-spring-boot")
include("frisboo-versions")
