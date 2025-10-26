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
    id("quality-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(project.file("docs/api"))
    }
}

dependencies {
//    dokka(projects.frisboo.frisbooCore)
}

allprojects {
    if (this != rootProject) {
        apply(plugin = "com.vanniktech.maven.publish")
    }
}
