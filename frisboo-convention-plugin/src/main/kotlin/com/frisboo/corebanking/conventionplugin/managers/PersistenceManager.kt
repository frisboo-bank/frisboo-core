package com.frisboo.corebanking.conventionplugin.managers

import com.frisboo.corebanking.conventionplugin.extensions.KotlinExtention
import com.frisboo.corebanking.conventionplugin.extensions.PersistenceExtension
import com.frisboo.corebanking.conventionplugin.utils.getLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

public class PersistenceManager(
    private val project: Project,
    private val ext: PersistenceExtension,
) {
    private val libs = project.getLibs()

    public fun configure() {
        if (!ext.enabled.get()) return

        project.dependencies {
            add("implementation", "org.springframework.boot:spring-boot-starter-data-r2dbc")
            add("implementation", "org.springframework.boot:spring-boot-starter-data-redis-reactive")

            add("runtimeOnly", "com.h2database:h2")
            add("runtimeOnly", "io.r2dbc:r2dbc-h2")
            add("runtimeOnly", "org.postgresql:postgresql")
            add("runtimeOnly", "org.postgresql:r2dbc-postgresql")
        }
    }
}
