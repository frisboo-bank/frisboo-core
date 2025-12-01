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
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

dependencies {
    implementation(project(":frisboo-core"))
    implementation(project(":frisboo-data"))

    arrayOf(
        libs.exposed.bom,
        libs.junit.bom,
        libs.kotest.bom,
        libs.reactor.bom,
        libs.spring.boot.bom,
        libs.testcontainers.bom,
    ).map {
        api(platform(it))
        testApi(platform(it))
    }

    arrayOf(
        libs.exposed.kotlin.datetime,
        libs.exposed.spring.boot.starter,
        libs.flyway.core,
        libs.flyway.database.postgresql,
        libs.kotest.assertions.core,
        libs.kotest.property,
        libs.kotest.property.arbs,
        libs.kotest.property.datetime,
        libs.kotlin.test.junit5,
        libs.kotlinx.coroutines.test,
        libs.mockk,
        libs.postgresql,
        libs.reactor.test,
        libs.spring.boot.starter.test,
        libs.spring.boot.testcontainers,
        libs.testcontainers.junit.jupiter,
        libs.testcontainers.postgresql,
    ).map {
        api(it)
        testApi(it)
    }

    testRuntimeOnly(libs.postgresql)
    testRuntimeOnly(libs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
