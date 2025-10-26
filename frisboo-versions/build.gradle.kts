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
    `version-catalog`
    alias(libs.plugins.dokka)
}

catalog {
    versionCatalog {
        from(files("libs.versions.toml"))
    }
}

// publishing {
//    publications {
//        create<MavenPublication>("versionCatalog") {
//            from(components["versionCatalog"])
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.version.toString()
//
//            pom {
//                name.set("frisboo-core-banking-version-catalog")
//                description.set("Shared library versions for Frisboo core banking services")
//            }
//        }
//    }
//    repositories {
//        mavenLocal()
//    }
// }
