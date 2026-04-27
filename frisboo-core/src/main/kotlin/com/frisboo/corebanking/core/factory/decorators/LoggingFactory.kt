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
package com.frisboo.corebanking.core.factory.decorators

import com.frisboo.corebanking.core.factory.contracts.AsyncFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.measureTimedValue

public class LoggingFactory<C : Any, T : Any>(
    private val delegate: AsyncFactory<C, T>,
) : AsyncFactory<C, T> {
    public companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun getOrCreate(
        name: String,
        config: C,
    ): T {
        logger.info { "getOrCreate($name, $config)" }
        val (instance, duration) =
            measureTimedValue {
                delegate.getOrCreate(name, config)
            }
        logger.info { "getOrCreate($name) completed in $duration" }
        return instance
    }

    override suspend fun evict(name: String) {
        logger.info { "evict($name)" }
        delegate.evict(name)
    }

    override suspend fun estimatedSize(): Long {
        val size = delegate.estimatedSize()
        logger.debug { "estimatedSize() = $size" }
        return size
    }
}
