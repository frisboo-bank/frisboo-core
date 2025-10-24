plugins {
    id("kotlin-conventions")
    alias(libs.plugins.plugin.publish)
}

description = "Gradle plugin that provides conventions for core banking apis"

gradlePlugin {
    plugins {
        create("frisbooConvention") {
            id = "com.frisboo.corebanking.frisboo-convention-plugin"
            displayName = "Frisboo Core Banking Convention Plugin"
            description = "Gradle plugin that provides conventions for core banking apis"
            tags = listOf(
                "frisboo",
                "firsboo-core-banking",
                "convention-plugin",
            )
            implementationClass = "com.frisboo.corebanking.conventionplugin.FrisbooCoreBankingConventionPlugin"
        }
    }
}

tasks.register("publishToLocal") {
    group = "Publishing"
    description = "Publishes the project to the local Maven repository"
    dependsOn("publishToMavenLocal")
}

tasks.withType<ValidatePlugins>().configureEach {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}
