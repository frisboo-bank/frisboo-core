package com.frisboo.corebanking.conventionplugin

public object DependencyConstants {
    public object Versions {
        // Core Framework
        public const val KOTLIN: String = "kotlin-version"
        public const val KOTLIN_LANGUAGE: String = "kotlin-language-version"
        public const val JVM_TARGET: String = "jvm-target-version"
        public const val FRISBOO_COREBANKING: String = "frisboo-corebanking-version"
        public const val SPRING_BOOT: String = "spring-boot-version"

        // Kotlin Ecosystem
        public const val KOTLINX_COROUTINES: String = "kotlinx-coroutines-version"
        public const val ARROWKT: String = "arrowkt-version"
        public const val JETBRAINS_ANNOTATIONS: String = "jetbrains-annotations-version"

        // Spring Ecosystem
        public const val SPRING_GRPC: String = "spring-grpc-version"
        public const val SPRINGDOC_OPENAPI: String = "springdoc-openapi-version"
        public const val REACTOR: String = "reactor-version"

        // Database & Persistence
        public const val EXPOSED: String = "exposed-version"
        public const val POSTGRESQL: String = "postgresql-version"
        public const val MONGODB: String = "mongodb-version"
        public const val H2: String = "h2-version"

        // gRPC & Protocol Buffers
        public const val GRPC_PROTOBUF: String = "grpc-protobuf-version"
        public const val GRPC_KOTLIN_STUB: String = "grpc-kotlin-stub-version"
        public const val PROTOBUF: String = "protobuf-version"
        public const val PROTOBUF_PLUGIN: String = "protobuf-plugin-version"

        // Testing
        public const val JUNIT: String = "junit-version"
        public const val KOTEST: String = "kotest-version"
        public const val MOCKK: String = "mockk-version"
        public const val TESTCONTAINERS: String = "testcontainers-version"

        // Code Quality & Tooling
        public const val DETEKT: String = "detekt-version"
        public const val KTLINT: String = "ktlint-version"
        public const val SPOTLESS: String = "spotless-version"
        public const val KOVER: String = "kover-version"
        public const val DOKKA: String = "dokka-version"
        public const val OWASP_CHECK: String = "owaspCheck-version"

        // Development Utilities
        public const val GRADLE_VERSIONS: String = "gradle-versions-version"
        public const val JACKSON: String = "jackson-version"
        public const val MAVEN_PUBLISH: String = "maven-publish-version"
        public const val PLUGIN_PUBLISH: String = "plugin-publish-version"

        // Miscellaneous
        public const val JGIT: String = "jgit-version"
        public const val RESTRICT_IMPORTS: String = "restrict-imports-version"
    }

    public object Libraries {
        // BOMs
        public const val FRISBOO_COREBANKING_BOM: String = "frisboo-corebanking-bom"
        public const val JACKSON_BOM: String = "jackson-bom"
        public const val JUNIT_BOM: String = "junit-bom"
        public const val KOTLIN_BOM: String = "kotlin-bom"
        public const val KOTLINX_COROUTINES_BOM: String = "kotlinx-coroutines-bom"
        public const val SPRING_BOOT_BOM: String = "spring-boot-bom"
        public const val TESTCONTAINERS_BOM: String = "testcontainers-bom"

        // Core Framework
        public const val COREBANKING_CORE: String = "corebanking-core"

        // Kotlin Core
        public const val KOTLIN_REFLECT: String = "kotlin-reflect"
        public const val KOTLIN_GRADLE_PLUGIN: String = "kotlin-gradle-plugin"
        public const val KOTLINX_COROUTINES_CORE: String = "kotlinx-coroutines-core"
        public const val KOTLINX_COROUTINES_REACTOR: String = "kotlinx-coroutines-reactor"
        public const val JETBRAINS_ANNOTATIONS: String = "jetbrains-annotations"

        // Spring Boot Starters
        public const val SPRING_BOOT_STARTER_WEBFLUX: String = "spring-boot-starter-webflux"
        public const val SPRING_BOOT_STARTER_VALIDATION: String = "spring-boot-starter-validation"
        public const val SPRING_BOOT_STARTER_ACTUATOR: String = "spring-boot-starter-actuator"
        public const val SPRING_BOOT_STARTER_DATA_MONGODB: String = "spring-boot-starter-data-mongodb"
        public const val SPRING_BOOT_STARTER_TEST: String = "spring-boot-starter-test"

        // Reactive & Reactor
        public const val REACTOR_BOM: String = "reactor-bom"
        public const val REACTOR_KOTLIN_EXTENSIONS: String = "reactor-kotlin-extensions"
        public const val REACTOR_TEST: String = "reactor-test"

        // Database & Persistence
        public const val EXPOSED_BOM: String = "exposed-bom"
        public const val EXPOSED_SPRING_BOOT_STARTER: String = "exposed-spring-boot-starter"
        public const val EXPOSED_JDBC: String = "exposed-jdbc"
        public const val POSTGRESQL: String = "postgresql"
        public const val MONGODB_DRIVER: String = "mongodb-driver"
        public const val H2: String = "h2"

        // gRPC & Protocol Buffers
        public const val GRPC_KOTLIN_STUB: String = "grpc-kotlin-stub"
        public const val GRPC_NETTY_SHADED: String = "grpc-netty-shaded"
        public const val GRPC_PROTOBUF: String = "grpc-protobuf"
        public const val PROTOBUF_KOTLIN: String = "protobuf-kotlin"
        public const val SPRING_GRPC_STARTER: String = "spring-grpc-starter"

        // API Documentation
        public const val SPRINGDOC_OPENAPI_BOM: String = "springdoc-openapi-bom"
        public const val SPRINGDOC_OPENAPI_STARTER_WEBFLUX_UI: String = "springdoc-openapi-starter-webflux-ui"

        // Testing
        public const val KOTEST_ASSERTIONS_CORE: String = "kotest-assertions-core"
        public const val KOTEST_RUNNER_JUNIT5: String = "kotest-runner-junit5"
        public const val MOCKK: String = "mockk"
        public const val TESTCONTAINERS_JUNIT_JUPITER: String = "testcontainers-junit-jupiter"
        public const val TESTCONTAINERS_POSTGRESQL: String = "testcontainers-postgresql"
        public const val TESTCONTAINERS_MONGODB: String = "testcontainers-mongodb"
        public const val JUNIT_PLATFORM_LAUNCHER: String = "junit-platform-launcher"

        // Serialization
        public const val JACKSON_MODULE_KOTLIN: String = "jackson-module-kotlin"

        // Code Quality Plugins (as libraries)
        public const val DETEKT_GRADLE_PLUGIN: String = "detekt-gradle-plugin"
        public const val SPOTLESS_GRADLE_PLUGIN: String = "spotless-gradle-plugin"
        public const val KOVER_GRADLE_PLUGIN: String = "kover-gradle-plugin"
        public const val OWASP_DEPCHECK_GRADLE_PLUGIN: String = "owasp-depcheck-gradle-plugin"

        // Miscellaneous
        public const val JGIT_GRADLE_PLUGIN: String = "jgit-gradle-plugin"
        public const val RESTRICT_IMPORTS_PLUGIN: String = "restrict-imports-plugin"
    }

    public object Plugins {
        // Core Framework
        public const val FRISBOO_COREBANKING_CONVENTION_PLUGIN: String = "frisboo-corebanking-convention-plugin"
        public const val KOTLIN_JVM: String = "kotlin-jvm"
        public const val KOTLIN_SPRING: String = "kotlin-spring"
        public const val SPRING_BOOT: String = "spring-boot"

        // API & Documentation
        public const val DOKKA: String = "dokka"
        public const val PROTOBUF: String = "protobuf"

        // Code Quality
        public const val DETEKT: String = "detekt"
        public const val SPOTLESS: String = "spotless"
        public const val KOVER: String = "kover"

        // Development & Publishing
        public const val MAVEN_PUBLISH: String = "maven-publish"
        public const val PLUGIN_PUBLISH: String = "plugin-publish"
        public const val GRADLE_VERSIONS: String = "gradle-versions"
    }
}
