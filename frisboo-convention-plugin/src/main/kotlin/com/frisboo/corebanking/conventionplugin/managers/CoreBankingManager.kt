package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.DependencyConstants
import com.frisboo.corebanking.conventionplugin.extensions.CoreBankingExtension
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import com.frisboo.corebanking.conventionplugin.utils.libraryOrThrow
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

public class CoreBankingManager(
    private val project: Project, private val ext: CoreBankingExtension,
) {
    private val libs = project.getLibs()

    public fun configure() {
        if (!ext.enabled.get()) return

        project.dependencies {
//            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.COREBANKING_CORE))
//            add("implementation", libs.libraryOrThrow(DependencyConstants.Libraries.COREBANKING_SPRING_BOOT))
        }
    }

}
