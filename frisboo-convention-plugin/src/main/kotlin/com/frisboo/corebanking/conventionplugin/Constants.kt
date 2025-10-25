package com.frisboo.corebanking.conventionplugin

public object Constants {
    public object Configurations {
        public object Bom {
            private const val BOM_PREFIX = "bom"
            public const val ENABLE_BOM: String = "${BOM_PREFIX}.enabled"
            public const val EXPOSED: String = "${BOM_PREFIX}.exposed.enabled"
            public const val JACKSON: String = "${BOM_PREFIX}.jackson.enabled"
            public const val JUNIT: String = "${BOM_PREFIX}.junit.enabled"
            public const val KOTLIN: String = "${BOM_PREFIX}.kotlin.enabled"
            public const val KOTLINX_COROUTINES: String = "${BOM_PREFIX}.kotlinxCoroutines.enabled"
            public const val SPRINGDOC_OPENAPI: String = "${BOM_PREFIX}.springdocOpenapi.enabled"
            public const val SPRING_BOOT: String = "${BOM_PREFIX}.springBoot.enabled"
            public const val TESTCONTAINERS: String = "${BOM_PREFIX}.testcontainers.enabled"
        }

        public object Quality {
            private const val QUALITY_PREFIX = "quality"
            public const val ENABLE_QUALITY: String = "${QUALITY_PREFIX}.enabled"
            public const val DETEKT_ENABLED: String = "${QUALITY_PREFIX}.detekt.enabled"
            public const val DOKKA_ENABLED: String = "${QUALITY_PREFIX}.dokka.enabled"
            public const val KOVER_ENABLED: String = "${QUALITY_PREFIX}.kover.enabled"
            public const val KOVER_THRESHOLDS_ENABLED: String = "${QUALITY_PREFIX}.kover.thresholds.enabled"
            public const val KTLINT_ENABLED: String = "${QUALITY_PREFIX}.ktlint.enabled"
            public const val OWASP_ENABLED: String = "${QUALITY_PREFIX}.owasp.enabled"
            public const val OWASP_FAIL_ON_VULNERABILITIES: String = "${QUALITY_PREFIX}.owasp.failOnVulnerabilities"
            public const val SPOTLESS_ENABLED: String = "${QUALITY_PREFIX}.spotless.enabled"
        }

        public object Kotlin {
            private const val KOTLIN_PREFIX = "kotlin"
            public const val ENABLE_KOTLIN: String = "${KOTLIN_PREFIX}.enabled"
            public const val ARROW_KT_ENABLED: String = "${KOTLIN_PREFIX}.arrow.enabled"
            public const val COROUTINES_ENABLED: String = "${KOTLIN_PREFIX}.coroutines.enabled"
            public const val COROUTINES_REACTOR_ENABLED: String = "${KOTLIN_PREFIX}.coroutinesReactor.enabled"
            public const val JETBRAINS_ANNOTATIONS_ENABLED: String = "${KOTLIN_PREFIX}.jetbrainsAnnotations.enabled"
            public const val KOTLIN_REFLECT_ENABLED: String = "${KOTLIN_PREFIX}.reflect.enabled"
        }

        public object Spring {
            private const val SPRING_PREFIX = "spring"
            public const val ENABLE_SPRING: String = "${SPRING_PREFIX}.enabled"
            public const val ACTUATOR_ENABLED: String = "${SPRING_PREFIX}.actuator.enabled"
            public const val DATA_MONGODB_ENABLED: String = "${SPRING_PREFIX}.dataMongodb.enabled"
            public const val SPRING_GRPC_ENABLED: String = "${SPRING_PREFIX}.grpc.enabled"
            public const val SPRING_TEST_ENABLED: String = "${SPRING_PREFIX}.test.enabled"
            public const val VALIDATION_ENABLED: String = "${SPRING_PREFIX}.validation.enabled"
            public const val WEBFLUX_ENABLED: String = "${SPRING_PREFIX}.webflux.enabled"
        }

        public object Database {
            private const val DATABASE_PREFIX = "db"
            public const val ENABLE_DATABASE: String = "${DATABASE_PREFIX}.enabled"
            public const val EXPOSED_ENABLED: String = "${DATABASE_PREFIX}.exposed.enabled"
            public const val FLYWAY_ENABLED: String = "${DATABASE_PREFIX}.flyway.enabled"
            public const val H2_ENABLED: String = "${DATABASE_PREFIX}.h2.enabled"
            public const val MONGODB_ENABLED: String = "${DATABASE_PREFIX}.mongodb.enabled"
            public const val POSTGRESQL_ENABLED: String = "${DATABASE_PREFIX}.postgresql.enabled"
        }

        public object Grpc {
            private const val GRPC_PREFIX = "grpc"
            public const val ENABLE_GRPC: String = "${GRPC_PREFIX}.enabled"
            public const val GRPC_KOTLIN_STUB_ENABLED: String = "${GRPC_PREFIX}.kotlinStub.enabled"
            public const val GRPC_NETTY_ENABLED: String = "${GRPC_PREFIX}.netty.enabled"
            public const val GRPC_PROTOBUF_ENABLED: String = "${GRPC_PREFIX}.protobuf.enabled"
            public const val PROTOBUF_KOTLIN_ENABLED: String = "${GRPC_PREFIX}.protobufKotlin.enabled"
            public const val PROTOBUF_PLUGIN_ENABLED: String = "${GRPC_PREFIX}.protobufPlugin.enabled"
        }

        public object Testing {
            private const val TESTING_PREFIX = "testing"
            public const val ENABLE_TESTING: String = "${TESTING_PREFIX}.enabled"
            public const val JUNIT_PLATFORM_LAUNCHER_ENABLED: String = "${TESTING_PREFIX}.junitPlatformLauncher.enabled"
            public const val KOTEST_ENABLED: String = "${TESTING_PREFIX}.kotest.enabled"
            public const val MOCKK_ENABLED: String = "${TESTING_PREFIX}.mockk.enabled"
            public const val REACTOR_TEST_ENABLED: String = "${TESTING_PREFIX}.reactorTest.enabled"
            public const val TESTCONTAINERS_ENABLED: String = "${TESTING_PREFIX}.testcontainers.enabled"
            public const val TESTCONTAINERS_MONGODB_ENABLED: String = "${TESTING_PREFIX}.testcontainers.mongodb.enabled"
            public const val TESTCONTAINERS_POSTGRESQL_ENABLED: String =
                "${TESTING_PREFIX}.testcontainers.postgresql.enabled"
        }

        public object Development {
            private const val DEVELOPMENT_PREFIX = "dev"
            public const val ENABLE_DEVELOPMENT: String = "${DEVELOPMENT_PREFIX}.enabled"
            public const val GRADLE_VERSIONS_ENABLED: String = "${DEVELOPMENT_PREFIX}.gradleVersions.enabled"
            public const val JACKSON_KOTLIN_MODULE_ENABLED: String = "${DEVELOPMENT_PREFIX}.jacksonKotlinModule.enabled"
        }

        public object Reactor {
            private const val REACTOR_PREFIX = "reactor"
            public const val ENABLE_REACTOR: String = "${REACTOR_PREFIX}.enabled"
            public const val REACTOR_KOTLIN_EXTENSIONS_ENABLED: String = "${REACTOR_PREFIX}.kotlinExtensions.enabled"
        }

        public object CoreFramework {
            private const val CORE_FRAMEWORK_PREFIX = "core"
            public const val ENABLE_CORE_FRAMEWORK: String = "${CORE_FRAMEWORK_PREFIX}.enabled"
            public const val FRISBOO_COREBANKING_ENABLED: String = "${CORE_FRAMEWORK_PREFIX}.frisboo.enabled"
        }
    }
}
