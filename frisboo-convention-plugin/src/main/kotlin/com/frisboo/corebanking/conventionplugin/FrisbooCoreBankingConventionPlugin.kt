package com.frisboo.corebanking.conventionplugin

import com.frisboo.corebanking.conventionplugin.managers.BomManager
import com.frisboo.corebanking.conventionplugin.managers.language.JavaManager
import com.frisboo.corebanking.conventionplugin.managers.language.KotlinManager
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.getVersionOrFail
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public class FrisbooCoreBankingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
//        repositories {
//            gradlePluginPortal()
//            mavenCentral()
//            mavenLocal()
//        }

        val libs = getLibs()
        val ext =
            extensions.create<FrisbooCoreBankingConventionExtension>("frisbooCoreBankingConventionExtension", libs)

        JavaManager(this).configure()
        KotlinManager(this).configure()

        project.afterEvaluate {
            try {
                BomManager(this, ext.bom).configure()
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
