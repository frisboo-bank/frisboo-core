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
package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.TestingExtension
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

public class TestingManager(
    private val project: Project,
    private val ext: TestingExtension,
) {
    private val libs = project.getLibs()

    public fun configure() {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            addKotlinTestingDependencies()
        }
    }

    private fun addKotlinTestingDependencies() {
        project.dependencies {
            add("testImplementation", libs.libraryOrThrow("kotest-assertions-core").get())
            add("testImplementation", libs.libraryOrThrow("mockk").get())
        }
    }
}
