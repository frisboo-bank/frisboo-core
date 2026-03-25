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

    arrayOf(
        baseLibs.spring.boot.bom,
        baseLibs.resilience4j.bom,
        baseLibs.opentelemetry.bom,
    ).forEach { api(platform(it)) }

    arrayOf(
        baseLibs.bucket4j.caffeine,
        baseLibs.bucket4j.core,
        baseLibs.bucket4j.lettuce,
        baseLibs.bucket4j.redis,
        baseLibs.caffeine,
        baseLibs.opentelemetry.spring.boot.starter,
        baseLibs.resilience4j.circuitbreaker,
        baseLibs.resilience4j.kotlin,
        baseLibs.spring.boot.autoconfigure,
        baseLibs.spring.boot.starter.actuator,
        baseLibs.spring.boot.starter.data.redis,
        baseLibs.spring.boot.starter.validation,
    ).forEach { api(it) }
}
