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
    implementation(project(":frisboo-core"))
    implementation(project(":frisboo-statemanager"))

    implementation(platform(baseLibs.resilience4j.bom))

    implementation(baseLibs.resilience4j.circuitbreaker)
    implementation(baseLibs.resilience4j.ratelimiter)
    implementation(baseLibs.resilience4j.kotlin)

    testImplementation(platform(baseLibs.junit.bom))
    testImplementation(platform(baseLibs.kotest.bom))

    testImplementation(project(":frisboo-tests"))
    testImplementation(baseLibs.kotest.property.arbs)
    testImplementation(baseLibs.kotest.extensions)
    testImplementation(baseLibs.kotest.assertions.core)
    testImplementation(baseLibs.kotest.assertions.arrow)
    testImplementation(baseLibs.kotlinx.coroutines.test)

    testRuntimeOnly(baseLibs.kotest.runner.junit5)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
