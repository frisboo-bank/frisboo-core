plugins {
    `jvm-test-suite`
    id("kotlin-conventions")
    alias(libs.plugins.plugin.publish)
}

description = "Gradle plugin that provides conventions for OpenAPI in Frisboo projects"

//gradlePlugin {
//    plugins {
//        create(
//            "frisbooConvention",
//            Action {
//                id = "com.frisboo.corebanking.openapi-convention-plugin"
//                displayName = "Frisboo Core Banking Convention Plugin for OpenApi"
//                description = "Gradle plugin that provides conventions for core banking apis OpenApi"
//                tags = listOf(
//                    "frisboo",
//                    "frisboo-core-banking",
//                    "convention-plugin",
//                    "openapi"
//                )
//                implementationClass = "com.frisboo.corebanking.FrisbooCoreBankingConventionPlugin"
//            },
//        )
//    }
//}
//
//tasks.register("publishToLocal") {
//    group = "Publishing"
//    description = "Publishes the project to the local Maven repository"
//    dependsOn("publishToMavenLocal")
//}
//
//tasks.withType<ValidatePlugins>().configureEach {
//    failOnWarning.set(true)
//    enableStricterValidation.set(true)
//}
