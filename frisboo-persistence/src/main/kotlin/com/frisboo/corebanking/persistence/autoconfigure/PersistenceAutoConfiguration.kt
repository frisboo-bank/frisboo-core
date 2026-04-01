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
package com.frisboo.corebanking.persistence.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Persistence auto-configuration.
 *
 * Registers [PersistenceProperties] and provides conditional [Configuration] markers that
 * downstream services can use with `@ConditionalOnBean` to gate their own persistence beans.
 *
 * Unlike [com.frisboo.corebanking.crypto.autoconfigure.CryptoAutoConfiguration], this module
 * does not create beans itself — it supplies shared schema migrations and Exposed extensions.
 * The marker configurations exist so that services can write:
 *
 * ```kotlin
 * @ConditionalOnBean(PersistenceAutoConfiguration.Postgres::class)
 * fun myDataSource(): DataSource = ...
 * ```
 */
@AutoConfiguration(after = [DataSourceAutoConfiguration::class])
@EnableConfigurationProperties(PersistenceProperties::class)
public open class PersistenceAutoConfiguration {
    @Configuration
    @ConditionalOnProperty(
        prefix = "frisboo.corebanking.persistence.postgres",
        name = ["enabled"],
        havingValue = "true",
    )
    public open class Postgres

    @Configuration
    @ConditionalOnProperty(
        prefix = "frisboo.corebanking.persistence.mongodb",
        name = ["enabled"],
        havingValue = "true",
    )
    public open class MongoDb
}
