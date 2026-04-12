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
    alias(baseLibs.plugins.dokka)
    alias(baseLibs.plugins.maven.publish)
}

dependencies {
    api(project(":frisboo-core"))

    api(platform(baseLibs.exposed.bom))
    api(platform(baseLibs.junit.bom))
    api(platform(baseLibs.kotest.bom))
    api(platform(baseLibs.reactor.bom))
    api(platform(baseLibs.spring.boot.bom))
    api(platform(baseLibs.testcontainers.bom))

    implementation(baseLibs.exposed.kotlin.datetime)
    implementation(baseLibs.exposed.spring.boot.starter)
    implementation(baseLibs.flyway.core)
    implementation(baseLibs.flyway.database.postgresql)
    implementation(baseLibs.postgresql)

    api(baseLibs.testcontainers.postgresql)
    api(baseLibs.testcontainers.toxiproxy)

    testApi(baseLibs.kotest.assertions.arrow)
    testApi(baseLibs.kotest.assertions.core)
    testApi(baseLibs.kotest.extensions)
    testApi(baseLibs.kotest.extensions.testcontainers)
    testApi(baseLibs.kotest.property)
    testApi(baseLibs.kotest.property.arbs)
    testApi(baseLibs.kotest.property.datetime)
    testApi(baseLibs.kotlin.test.junit5)
    testApi(baseLibs.kotlinx.coroutines.test)
    testApi(baseLibs.mockk)
    testApi(baseLibs.reactor.test)
    testApi(baseLibs.spring.boot.starter.test)
    testApi(baseLibs.spring.boot.testcontainers)
    testApi(baseLibs.testcontainers.junit.jupiter)
    testApi(baseLibs.toxiproxy)

    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
