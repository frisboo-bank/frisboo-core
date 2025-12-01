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
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

dependencies {
    arrayOf(
        libs.arrow.kt.bom,
        libs.kotlin.bom,
        libs.kotlinx.coroutines.bom,
        libs.jackson.bom,
        libs.reactor.bom,
        libs.spring.boot.bom,
    ).map { api(platform(it)) }

    arrayOf(
        libs.arrow.kt.core,
        libs.arrow.kt.coroutines,
        libs.bundles.serialization,
        libs.jetbrains.annotations,
        libs.kotlin.logging,
        libs.kotlin.reflect,
        libs.kotlinx.coroutines.core,
        libs.kotlinx.coroutines.debug,
        libs.kotlinx.coroutines.reactor,
        libs.kotlinx.datetime,
        libs.reactor.kotlin.extensions,
        libs.spring.boot.autoconfigure,
    ).map { api(it) }

    testApi(platform(libs.junit.bom))
    testApi(platform(libs.kotest.bom))

    testRuntimeOnly(libs.kotest.runner.junit5)
}

tasks.test {
    useJUnitPlatform()
}
