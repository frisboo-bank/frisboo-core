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
import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

public class CachingFactory<C : Any, T : Any>(
    private val cache: FactoryCache<String, T>,
    private val delegate: AsyncFactory<C, T>,
    private val matchesConfig: (T, C) -> Boolean,
) : AsyncFactory<C, T> {

    public companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val creationLocks = ConcurrentHashMap<String, Mutex>()

    init {
        cache.setEvictionListener { key, _ ->
            key?.let { creationLocks.remove(it) }
        }
    }

    override suspend fun getOrCreate(name: String, config: C): T {
        val existing = cache.getIfPresent(name)
        if (existing != null && matchesConfig(existing, config)) {
            return existing
        }

        val lock = creationLocks.computeIfAbsent(name) { Mutex() }
        return lock.withLock {
            // Re-check after acquiring lock
            val current = cache.getIfPresent(name)
            if (current != null && matchesConfig(current, config)) {
                return@withLock current
            }

            if (current != null) {
                logger.warn {
                    "Instance '$name' already exists with a different configuration — " +
                            "replacing with the new config. This indicates a config drift that should be fixed."
                }
            }

            val newInstance = delegate.getOrCreate(name, config)
            cache.put(name, newInstance)
            newInstance
        }
    }

    override suspend fun evict(name: String) {
        val lock = creationLocks.computeIfAbsent(name) { Mutex() }
        lock.withLock {
            cache.invalidate(name)
            creationLocks.remove(name)
        }
    }

    override suspend fun estimatedSize(): Long = cache.estimatedSize()
}

