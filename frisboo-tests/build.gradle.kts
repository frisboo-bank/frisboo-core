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
    implementation(project(":frisboo-data"))

    arrayOf(
        baseLibs.exposed.bom,
        baseLibs.junit.bom,
        baseLibs.kotest.bom,
        baseLibs.reactor.bom,
        baseLibs.spring.boot.bom,
        baseLibs.testcontainers.bom,
    ).map {
        api(platform(it))
        testApi(platform(it))
    }

    arrayOf(
        baseLibs.exposed.kotlin.datetime,
        baseLibs.exposed.spring.boot.starter,
        baseLibs.flyway.core,
        baseLibs.flyway.database.postgresql,
        baseLibs.kotest.assertions.core,
        baseLibs.kotest.extensions,
        baseLibs.kotest.property,
        baseLibs.kotest.property.arbs,
        baseLibs.kotest.property.datetime,
        baseLibs.kotlin.test.junit5,
        baseLibs.kotlinx.coroutines.test,
        baseLibs.mockk,
        baseLibs.postgresql,
        baseLibs.reactor.test,
        baseLibs.spring.boot.starter.test,
        baseLibs.spring.boot.testcontainers,
        baseLibs.testcontainers.junit.jupiter,
        baseLibs.testcontainers.postgresql,
    ).map {
        api(it)
        testApi(it)
    }

    testRuntimeOnly(baseLibs.postgresql)
    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
