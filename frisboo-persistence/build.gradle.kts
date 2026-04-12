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
    api(project(":frisboo-data"))

    api(platform(baseLibs.spring.boot.bom))
    api(platform(baseLibs.exposed.bom))

    api(baseLibs.exposed.spring.boot.starter)
    api(baseLibs.exposed.kotlin.datetime)

    implementation(baseLibs.testcontainers.redis)

    implementation(baseLibs.spring.boot.autoconfigure)
    implementation(baseLibs.lettuce.core)

    testApi(platform(baseLibs.junit.bom))
    testApi(platform(baseLibs.kotest.bom))
    testApi(platform(baseLibs.testcontainers.bom))

    testApi(baseLibs.kotest.assertions.core)
    testApi(baseLibs.kotest.assertions.arrow)
    testApi(baseLibs.kotest.property)
    testApi(baseLibs.kotest.property.arbs)

    testApi(baseLibs.spring.boot.testcontainers)
    testApi(baseLibs.testcontainers.junit.jupiter)
    testApi(baseLibs.testcontainers.postgresql)

    testApi(baseLibs.flyway.core)
    testApi(baseLibs.flyway.database.postgresql)
    testApi(baseLibs.kotlinx.coroutines.test)

    testRuntimeOnly(baseLibs.postgresql)
    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
