///*
// * Copyright 2025 Frisboo Bank
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing
// * permissions and limitations under the License.
// */
//package com.frisboo.corebanking.tests.testcontainers
//
//import org.flywaydb.core.Flyway
//import org.jetbrains.exposed.v1.core.StdOutSqlLogger
//import org.jetbrains.exposed.v1.jdbc.Database
//import org.jetbrains.exposed.v1.jdbc.transactions.transaction
//import org.junit.jupiter.api.TestInstance
//import org.testcontainers.containers.PostgreSQLContainer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
//
//@Testcontainers
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public open class WithPostgresDatabase(
//    private val dockerImageName: String = "postgres:latest",
//    private val databaseName: String,
//    private val username: String = "postgres",
//    private val password: String = "postgres",
//    private val migrationLocations: Array<String> = arrayOf("classpath:db/migration/postgres"),
//    private val migrationPlaceholders: Map<String, String> =
//        mapOf(
//            "inbox_table_name" to "inbox",
//            "sagas_table_name" to "sagas",
//            "outbox_table_name" to "outbox",
//        ),
//) {
//    @Container
//    public lateinit var container: PostgreSQLContainer<*>
//        private set
//    public lateinit var flyway: Flyway
//        private set
//    public lateinit var database: Database
//        private set
//
//    public fun start(): PostgreSQLContainer<*> {
//        if (::container.isInitialized && container.isRunning) return container
//
//        container =
//            PostgreSQLContainer<Nothing>(dockerImageName).apply {
//                withDatabaseName(databaseName)
//                withUsername(username)
//                withPassword(password)
//                start()
//            }
//
//        migrateDatabase()
//        connectWithExposed()
//
//        return container
//    }
//
//    public fun stop() {
//        if (!::container.isInitialized) return
//        container.stop()
//    }
//
//    private fun migrateDatabase() {
//        if (!::container.isInitialized) return
//
//        flyway =
//            Flyway
//                .configure()
//                .apply {
//                    dataSource(container.jdbcUrl, container.username, container.password)
//                    locations(*migrationLocations)
//                    placeholders(migrationPlaceholders)
//                }.load()
//
//        flyway.migrate()
//        flyway.validate()
//    }
//
//    private fun connectWithExposed() {
//        if (!::container.isInitialized) return
//
//        database =
//            Database.connect(
//                url = container.jdbcUrl,
//                user = container.username,
//                password = container.password,
//            )
//
//        transaction {
//            addLogger(StdOutSqlLogger)
//        }
//    }
//}
