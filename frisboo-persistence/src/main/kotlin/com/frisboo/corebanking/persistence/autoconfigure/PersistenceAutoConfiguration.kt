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
 */
@AutoConfiguration(after = [DataSourceAutoConfiguration::class])
@EnableConfigurationProperties(PersistenceProperties::class)
public class PersistenceAutoConfiguration {
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
