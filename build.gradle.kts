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
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kover)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(project.file("docs/api"))
    }
}

dependencies{
    dokka(projects.core.frisbooCore)
    dokka(projects.core.frisbooData)
    dokka(projects.core.frisbooGrpc)
    dokka(projects.core.frisbooHttp)
    dokka(projects.core.frisbooMessaging)
    dokka(projects.core.frisbooPersistence)
    dokka(projects.core.frisbooTests)

    kover(project(":frisboo-core"))
    kover(project(":frisboo-grpc"))
    kover(project(":frisboo-http"))
    kover(project(":frisboo-messaging"))
    kover(project(":frisboo-persistence"))
    kover(project(":frisboo-tests"))
}

allprojects {
    if (this != rootProject) {
        apply(plugin = "com.vanniktech.maven.publish")
    }
}
