package com.frisboo.corebanking.conventions

plugins {
    id("org.openapi.generator")
//    id("org.springdoc.openapi-gradle-plugin")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

private val namespace = "${project.group}.${project.name}"
private val schema = "${layout.projectDirectory}/src/api/schemas/customer_service.yaml"

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(schema)
    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
    packageName.set(namespace)
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useSwaggerUI" to "false",
            "useTags" to "true",
            "documentationProvider" to "springdoc",
        )
    )
}

openApiValidate {
    inputSpec.set(schema)
}

// Compile Task Dependencies
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

dependencies {
}
