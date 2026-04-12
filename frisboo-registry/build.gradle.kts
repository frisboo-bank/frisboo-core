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
    api(project(":frisboo-crypto"))
    api(project(":frisboo-persistence"))

    implementation(platform(baseLibs.kotlin.bom))
    implementation(platform(baseLibs.kotlinx.coroutines.bom))

    implementation(baseLibs.caffeine)
    implementation(baseLibs.kotlinx.coroutines.core)
    implementation(baseLibs.lettuce.core)

    testImplementation(platform(baseLibs.junit.bom))
    testImplementation(platform(baseLibs.kotest.bom))
    testImplementation(platform(baseLibs.testcontainers.bom))

    testImplementation(project(":frisboo-tests"))

    testImplementation(baseLibs.kotest.assertions.arrow)
    testImplementation(baseLibs.kotest.extensions)
    testImplementation(baseLibs.kotest.property.arbs)
    testImplementation(baseLibs.kotest.property.datetime)
    testImplementation(baseLibs.kotlin.test.junit5)
    testImplementation(baseLibs.kotest.extensions.testcontainers)
    testImplementation(baseLibs.testcontainers.redis)

    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
