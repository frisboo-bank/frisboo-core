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
    api(platform(baseLibs.arrow.kt.bom))
    api(platform(baseLibs.kotlin.bom))
    api(platform(baseLibs.kotlinx.coroutines.bom))
    api(platform(baseLibs.jackson.bom))
    api(platform(baseLibs.reactor.bom))
    api(platform(baseLibs.spring.boot.bom))

    api(baseLibs.arrow.kt.core)
    api(baseLibs.arrow.kt.coroutines)
    api(baseLibs.bundles.serialization)
    api(baseLibs.caffeine)
    api(baseLibs.caffeine.coroutines)
    api(baseLibs.jetbrains.annotations)
    api(baseLibs.kotlin.logging)
    api(baseLibs.kotlin.reflect)
    api(baseLibs.kotlinx.coroutines.core)
    api(baseLibs.kotlinx.coroutines.debug)
    api(baseLibs.kotlinx.coroutines.reactor)
    api(baseLibs.kotlinx.datetime)
    api(baseLibs.reactor.kotlin.extensions)
    api(baseLibs.spring.boot.autoconfigure)

    testImplementation(platform(baseLibs.junit.bom))
    testImplementation(platform(baseLibs.kotest.bom))
    testImplementation(baseLibs.kotest.assertions.core)
    testImplementation(baseLibs.kotest.property.arbs)
    testImplementation(baseLibs.kotlin.test.junit5)

    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
