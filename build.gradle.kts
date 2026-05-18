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
    alias(baseLibs.plugins.kover)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(project.file("docs/api"))
    }
}

dependencies {
    dokka(project(":frisboo-auth"))
    dokka(project(":frisboo-config"))
    dokka(project(":frisboo-coordination"))
    dokka(project(":frisboo-core"))
    dokka(project(":frisboo-crypto"))
    dokka(project(":frisboo-data"))
    dokka(project(":frisboo-grpc"))
    dokka(project(":frisboo-http"))
    dokka(project(":frisboo-messaging"))
    dokka(project(":frisboo-observability"))
    dokka(project(":frisboo-persistence"))
    dokka(project(":frisboo-quota"))
    dokka(project(":frisboo-resilience"))
    dokka(project(":frisboo-statemanager"))
    dokka(project(":frisboo-tests"))

    kover(project(":frisboo-auth"))
    kover(project(":frisboo-config"))
    kover(project(":frisboo-coordination"))
    kover(project(":frisboo-core"))
    kover(project(":frisboo-crypto"))
    kover(project(":frisboo-data"))
    kover(project(":frisboo-grpc"))
    kover(project(":frisboo-http"))
    kover(project(":frisboo-messaging"))
    kover(project(":frisboo-observability"))
    kover(project(":frisboo-persistence"))
    kover(project(":frisboo-quota"))
    kover(project(":frisboo-resilience"))
    kover(project(":frisboo-statemanager"))
}

