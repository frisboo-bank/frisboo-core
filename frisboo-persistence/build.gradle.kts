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
    id("kotlin-conventions")
    id("java-test-fixtures")
    alias(baseLibs.plugins.dokka)
    alias(baseLibs.plugins.maven.publish)
}

dependencies {
    api(project(":frisboo-core"))
    api(project(":frisboo-crypto"))
    api(project(":frisboo-data"))

    api(platform(baseLibs.spring.boot.bom))
    api(platform(baseLibs.exposed.bom))

    api(baseLibs.lettuce.core)
    api(baseLibs.exposed.spring.boot.starter)
    api(baseLibs.exposed.kotlin.datetime)

    implementation(baseLibs.spring.boot.autoconfigure)

    testApi(platform(baseLibs.junit.bom))
    testApi(platform(baseLibs.kotest.bom))
    testApi(platform(baseLibs.testcontainers.bom))
    testApi(baseLibs.kotest.assertions.core)
    testApi(baseLibs.kotest.assertions.arrow)
    testApi(baseLibs.kotest.property)
    testApi(baseLibs.kotest.property.arbs)
    testApi(baseLibs.testcontainers.junit.jupiter)
    testApi(baseLibs.kotlinx.coroutines.test)
    testApi(baseLibs.bundles.serialization)
    testRuntimeOnly(baseLibs.postgresql)
    testRuntimeOnly(baseLibs.kotest.runner.junit5)

    // Test fixtures dependencies
    testImplementation(testFixtures(project(":frisboo-persistence")))
    testFixturesImplementation(platform(baseLibs.testcontainers.bom))
    testFixturesApi(baseLibs.lettuce.core)
    testFixturesApi(baseLibs.kotlinx.coroutines.core)
    testFixturesApi(baseLibs.testcontainers.postgresql)
    testFixturesApi(baseLibs.testcontainers.redis)
    testFixturesApi(baseLibs.flyway.core)
    testFixturesApi(baseLibs.flyway.database.postgresql)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
