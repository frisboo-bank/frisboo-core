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
plugins {
    base
    id("kotlin-conventions")
    id("quality-conventions")
    alias(baseLibs.plugins.dokka)
    alias(baseLibs.plugins.maven.publish)
    alias(baseLibs.plugins.kover)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(project.file("docs/api"))
    }
}

dependencies {
    dokka(projects.core.frisbooAuth)
    dokka(projects.core.frisbooConfig)
    dokka(projects.core.frisbooCore)
    dokka(projects.core.frisbooCoordination)
    dokka(projects.core.frisbooCrypto)
    dokka(projects.core.frisbooData)
    dokka(projects.core.frisbooGrpc)
    dokka(projects.core.frisbooHttp)
    dokka(projects.core.frisbooMessaging)
    dokka(projects.core.frisbooObservability)
    dokka(projects.core.frisbooPersistence)
    dokka(projects.core.frisbooQuota)
    dokka(projects.core.frisbooResilience)
    dokka(projects.core.frisbooTests)

    kover(project(":frisboo-auth"))
    kover(project(":frisboo-config"))
    kover(project(":frisboo-core"))
    kover(project(":frisboo-coordination"))
    kover(project(":frisboo-crypto"))
    kover(project(":frisboo-grpc"))
    kover(project(":frisboo-http"))
    kover(project(":frisboo-messaging"))
    kover(project(":frisboo-observability"))
    kover(project(":frisboo-persistence"))
    kover(project(":frisboo-quota"))
    kover(project(":frisboo-resilience"))
    kover(project(":frisboo-tests"))
}

allprojects {
    if (this == rootProject) {
        return@allprojects
    }

    apply(plugin = "com.vanniktech.maven.publish")

    this@allprojects.publishing {
        repositories {
            maven {
                name = "FrisbooGitHubPackages"
                url = uri("https://maven.pkg.github.com/jolafrite/frisboo-core-banking")
                credentials {
                    username =
                        project.findProperty("frisboo.gpr.user") as String? ?: System.getenv("FRISBOO_GPR_USERNAME")
                    password = project.findProperty("frisboo.gpr.key") as String? ?: System.getenv("FRISBOO_GPR_TOKEN")
                }
            }
        }
    }
}
