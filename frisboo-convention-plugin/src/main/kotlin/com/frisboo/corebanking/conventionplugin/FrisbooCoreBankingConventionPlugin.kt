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
package com.frisboo.corebanking.conventionplugin

import com.frisboo.corebanking.conventionplugin.extensions.PluginExtension
import com.frisboo.corebanking.conventionplugin.managers.BomManager
import com.frisboo.corebanking.conventionplugin.managers.CoreBankingManager
import com.frisboo.corebanking.conventionplugin.managers.KotlinManager
import com.frisboo.corebanking.conventionplugin.managers.RestrictImportsManager
import com.frisboo.corebanking.conventionplugin.managers.SpringBootManager
import com.frisboo.corebanking.conventionplugin.managers.TestingManager
import com.frisboo.corebanking.conventionplugin.managers.language.JavaLanguage
import com.frisboo.corebanking.conventionplugin.managers.language.KotlinLanguage
import com.frisboo.corebanking.conventionplugin.managers.quality.QualityManager
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.getVersionOrFail
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.repositories

public class FrisbooCoreBankingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            mavenLocal()
        }

        val libs = getLibs()
        val ext = extensions.create<PluginExtension>("frisbooCoreBankingConventionExtension", libs)

        JavaLanguage(this).configure()
        KotlinLanguage(this).configure()

        project.afterEvaluate {
            try {
                BomManager(this, ext.bom).configure()
                RestrictImportsManager(this, ext).configure()
                KotlinManager(this, ext.kotlin).configure()
                QualityManager(this, ext.quality).configure()
                KotlinManager(this, ext.kotlin).configure()
                TestingManager(this, ext.testing).configure()
                SpringBootManager(this, ext.springBoot).configure()
                CoreBankingManager(this, ext.coreBanking).configure()
            } catch (e: IllegalStateException) {
                error("Failed to configure Frisboo Core Banking Convention Plugin: ${e.message}")
            }
        }

        tasks.register("frisbooCoreBankingConventionInfo") { t ->
            group = "Help"
            description = "Displays information about the Frisboo Core Banking Convention Plugin"

            val version = libs.getVersionOrFail("frisboo-corebanking")

            t.doLast {
                println("Frisboo Core Banking Convention Plugin Applied: $version")
            }
        }
    }
}
