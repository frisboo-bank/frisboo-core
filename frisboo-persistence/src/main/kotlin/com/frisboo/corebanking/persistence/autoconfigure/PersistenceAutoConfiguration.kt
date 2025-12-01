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

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Configuration

@AutoConfiguration(after = [DataSourceAutoConfiguration::class])
public open class PersistenceAutoConfiguration {
    @Configuration
    @ConditionalOnProperty(value = ["frisboo.corebanking.persistence.postgres.enabled"], havingValue = "true")
    public open class Postgres {
        @Value($$"${frisboo.corebanking.persistence.postgres.enabled:false}")
        private var enabled: Boolean = false

        public fun isEnabled(): Boolean = enabled
    }

    @Configuration
    @ConditionalOnProperty(value = ["frisboo.corebanking.persistence.mongodb.enabled"], havingValue = "true")
    public open class Inbox {
        @Value($$"${frisboo.corebanking.persistence.mongodb.enabled:false}")
        private var enabled: Boolean = false

        public fun isEnabled(): Boolean = enabled
    }
}
