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
package com.frisboo.corebanking.core.factory

import com.frisboo.corebanking.core.factory.adapters.AsyncCacheFactory
import com.frisboo.corebanking.core.factory.contracts.AsyncFactory
import com.frisboo.corebanking.core.factory.decorators.CachingFactory
import com.frisboo.corebanking.core.factory.decorators.LoggingFactory
import com.frisboo.corebanking.core.factory.decorators.MetricsFactory
import com.frisboo.corebanking.core.factory.models.AsyncFactoryCachingConfig
import com.frisboo.corebanking.core.factory.models.AsyncFactoryLoggingConfig
import com.frisboo.corebanking.core.factory.models.AsyncFactoryMetricsConfig

public object AsyncFactoryBuilder {
    public fun <C : Any, T : Any> builder(
        constructor: suspend (name: String, config: C) -> T,
        caching: AsyncFactoryCachingConfig<C, T>? = null,
        logging: AsyncFactoryLoggingConfig? = null,
        metrics: AsyncFactoryMetricsConfig? = null,
    ): AsyncFactory<C, T> {
        var factory: AsyncFactory<C, T> =
            object : AsyncFactory<C, T> {
                override suspend fun getOrCreate(
                    name: String,
                    config: C,
                ): T = constructor(name, config)

                override suspend fun evict(name: String) = Unit

                override suspend fun estimatedSize(): Long = 0
            }

        if (caching?.enabled == true) {
            val cache =
                AsyncCacheFactory.createCaffeine<String, T>(
                    maxSize = caching.maxSize,
                    expireAfterAccess = caching.expireAfterAccess,
                    recordStats = caching.recordStats,
                )
            factory =
                CachingFactory<C, T>(
                    cache = cache,
                    delegate = factory,
                    matchesConfig = caching.matchesConfig,
                )
        }

        if (logging?.enabled == true) {
            factory = LoggingFactory(delegate = factory)
        }

        if (metrics?.enabled == true) {
            factory = MetricsFactory(delegate = factory)
        }

        return factory
    }
}
