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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "frisboo-bom",
    "frisboo-core",
    "frisboo-http",
    "frisboo-openapi",
    "frisboo-openapi:convention-plugin",
    "frisboo-spring-boot",
    "frisboo-transaction",
)

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.3"
//}

// @Suppress("UnstableApiUsage")
// toolchainManagement {
//    jvm {
//        javaRepositories {
//            repository("foojay") {
//                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
//            }
//        }
//    }
// }

// gitHooks {
//    commitMsg { conventionalCommits() }
//    createHooks()
// }

@Suppress("UnstableApiUsage") dependencyResolutionManagement {
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
