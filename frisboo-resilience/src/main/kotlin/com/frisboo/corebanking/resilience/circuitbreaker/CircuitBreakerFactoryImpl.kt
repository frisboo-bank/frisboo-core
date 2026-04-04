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
package com.frisboo.corebanking.resilience.circuitbreaker

import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j.createResilience4jBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerConfigSource
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerFactory
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.github.benmanes.caffeine.cache.Caffeine
import dev.hsbrysk.caffeine.CoroutineCache
import dev.hsbrysk.caffeine.buildCoroutine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * @param stateRegistry persistent store for circuit breaker state across restarts.
 * @param coroutineScope scope for background state-persistence coroutines.
 * @param maxBreakers upper bound on cached breaker instances. When exceeded, the
 *   least-recently-accessed entry is evicted. Evicted breakers are recreated on
 *   next [create] call with their persisted state restored from [stateRegistry].
 *   Default is 10 000 — sufficient for most deployments.
 */
public class CircuitBreakerFactoryImpl(
    private val stateRegistry: Registry<String, String>,
    private val coroutineScope: CoroutineScope,
    private val maxBreakers: Long = 10_000,
    private val configSource: CircuitBreakerConfigSource? = null,
) : CircuitBreakerFactory {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val breakers: CoroutineCache<String, CachedEntry> =
        Caffeine
            .newBuilder()
            .maximumSize(maxBreakers)
            .expireAfterAccess(30.minutes.toJavaDuration())
            .buildCoroutine()

    override suspend fun create(
        name: String,
        config: CircuitBreakerConfig,
    ): CircuitBreaker {
        val existing = breakers.getIfPresent(name)
        if (existing != null && existing.config == config) {
            return existing.breaker
        }
        if (existing != null) {
            logger.warn {
                "CircuitBreaker '$name' already exists with a different configuration — " +
                    "replacing with the new config. This indicates a config drift that should be fixed."
            }
        }
        val newEntry = CachedEntry(
            breaker = createResilience4jBreaker(name, config, stateRegistry, coroutineScope),
            config = config,
        )
        breakers.put(name, newEntry)
        return newEntry.breaker
    }

    override suspend fun create(name: String): CircuitBreaker {
        val source = checkNotNull(configSource) {
            "No CircuitBreakerConfigSource configured. " +
                "Either provide a config source or call create(name, config) with explicit configuration."
        }
        val config = checkNotNull(source.resolve(name)) {
            "No circuit breaker configuration found for '$name'. " +
                "Define it in your configuration source or call create(name, config) with explicit configuration."
        }
        return create(name, config)
    }

    private data class CachedEntry(
        val breaker: CircuitBreaker,
        val config: CircuitBreakerConfig,
    )
}
