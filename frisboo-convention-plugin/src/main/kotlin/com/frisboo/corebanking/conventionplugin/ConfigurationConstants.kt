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

public object ConfigurationConstants {
    public object Plugin {
        public const val FRISBOO_COREBANKING_VERSION: String = "frisboo-corebanking-version"
        public const val TEST_JVM_ARGS: String = "test.jvmargs"
        public const val PUBLISH_TEST_JAR: String = "publish.testJar"
    }

    public object Bom {
        private const val BOM_PREFIX = "bom"
        public const val ENABLE_BOM: String = "${BOM_PREFIX}.enabled"
        public const val ARROW_KT: String = "${BOM_PREFIX}.arrowKt.enabled"
        public const val CORE_BANKING: String = "${BOM_PREFIX}.coreBanking.enabled"
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

    public object SpringBoot {
        private const val SPRING_PREFIX = "spring"
        public const val ENABLE_SPRING: String = "${SPRING_PREFIX}.enabled"
        public const val ACTUATOR_ENABLED: String = "${SPRING_PREFIX}.actuator.enabled"
        public const val DATA_MONGODB_ENABLED: String = "${SPRING_PREFIX}.dataMongodb.enabled"
        public const val SPRING_GRPC_ENABLED: String = "${SPRING_PREFIX}.grpc.enabled"
        public const val SPRING_TEST_ENABLED: String = "${SPRING_PREFIX}.test.enabled"
        public const val VALIDATION_ENABLED: String = "${SPRING_PREFIX}.validation.enabled"
        public const val WEBFLUX_ENABLED: String = "${SPRING_PREFIX}.webflux.enabled"
    }

    public object Persistence {
        private const val PERSISTENCE_PREFIX = "persistence"
        public const val ENABLE_PERSISTENCE: String = "${PERSISTENCE_PREFIX}.enabled"
        public const val EXPOSED_ENABLED: String = "${PERSISTENCE_PREFIX}.exposed.enabled"
        public const val FLYWAY_ENABLED: String = "${PERSISTENCE_PREFIX}.flyway.enabled"
        public const val H2_ENABLED: String = "${PERSISTENCE_PREFIX}.h2.enabled"
        public const val MONGODB_ENABLED: String = "${PERSISTENCE_PREFIX}.mongodb.enabled"
        public const val POSTGRESQL_ENABLED: String = "${PERSISTENCE_PREFIX}.postgresql.enabled"
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

    public object CoreBanking {
        private const val CORE_FRAMEWORK_PREFIX = "coreBanking"
        public const val ENABLE_COREBANKING: String = "${CORE_FRAMEWORK_PREFIX}.enabled"
        public const val ENABLE_COREBANKING_CORE: String = "${CORE_FRAMEWORK_PREFIX}.core.enabled"
        public const val ENABLE_COREBANKING_SPRING_BOOT: String = "${CORE_FRAMEWORK_PREFIX}.springBoot.enabled"
    }

    public object Telemetry {
        private const val TELEMETRY_PREFIX = "telemetry"
        public const val ENABLE_TELEMETRY: String = "${TELEMETRY_PREFIX}.enabled"
        public const val OPENTELEMETRY_ENABLED: String = "${TELEMETRY_PREFIX}.opentelemetry.enabled"
        public const val OPENTELEMETRY_EXPORTER_OTLP_ENABLED: String =
            "${TELEMETRY_PREFIX}.opentelemetryExporterOtlp.enabled"
    }

    public object Messaging {
        private const val MESSAGING_PREFIX = "messaging"
        public const val ENABLE_MESSAGING: String = "${MESSAGING_PREFIX}.enabled"
        public const val KAFKA_ENABLED: String = "${MESSAGING_PREFIX}.kafka.enabled"
    }

    public object Openapi {
        private const val OPENAPI_PREFIX = "openapi"
        public const val ENABLE_OPENAPI: String = "${OPENAPI_PREFIX}.enabled"
        public const val SPRINGDOC_OPENAPI_WEBFLUX_ENABLED: String = "${OPENAPI_PREFIX}.springdocOpenapiWebflux.enabled"
    }
}
