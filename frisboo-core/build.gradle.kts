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
    alias(baseLibs.plugins.kover)
    alias(baseLibs.plugins.dokka)
    alias(baseLibs.plugins.maven.publish)
}

dependencies {
    arrayOf(
        baseLibs.arrow.kt.bom,
        baseLibs.kotlin.bom,
        baseLibs.kotlinx.coroutines.bom,
        baseLibs.jackson.bom,
        baseLibs.reactor.bom,
        baseLibs.spring.boot.bom,
    ).forEach { api(platform(it)) }

    arrayOf(
        baseLibs.arrow.kt.core,
        baseLibs.arrow.kt.coroutines,
        baseLibs.bundles.serialization,
        baseLibs.jetbrains.annotations,
        baseLibs.kotlin.logging,
        baseLibs.kotlin.reflect,
        baseLibs.kotlinx.coroutines.core,
        baseLibs.kotlinx.coroutines.debug,
        baseLibs.kotlinx.coroutines.reactor,
        baseLibs.kotlinx.datetime,
        baseLibs.reactor.kotlin.extensions,
        baseLibs.spring.boot.autoconfigure,
    ).forEach { api(it) }

    testApi(platform(baseLibs.junit.bom))
    testApi(platform(baseLibs.kotest.bom))

    testApi(baseLibs.kotest.property.arbs)

    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.test {
    useJUnitPlatform()
}
