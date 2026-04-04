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
package com.frisboo.corebanking.resilience.ratelimiter

import com.frisboo.corebanking.core.factory.AsyncFactoryBuilder
import com.frisboo.corebanking.core.factory.models.AsyncFactoryCachingConfig
import com.frisboo.corebanking.core.factory.models.AsyncFactoryLoggingConfig
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j.createResilience4jBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerPersistenceContext
import com.frisboo.corebanking.resilience.ratelimiter.adapters.resilience4j.createResilience4jLimiter
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiter
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterConfigSource
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterFactory
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterPersistenceContext
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalListener
import dev.hsbrysk.caffeine.CoroutineCache
import dev.hsbrysk.caffeine.buildCoroutine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Create and manage rate limiters with automatic caching and optional external configuration source.
 *
 * @param stateRegistry Registry for persisting rate limiter state across instances.
 * @param maxLimiters Maximum number of rate limiters to cache before evicting old ones.
 * @param expireAfterAccess Duration after which an unused rate limiter will be evicted from the cache.
 * @param configSource Optional source for resolving rate limiter configurations by name.
 */
public class RateLimiterFactoryImpl(
    private val stateRegistry: Registry<String, String>,
    private val maxLimiters: Long = 10_000,
    private val expireAfterAccess: Duration = 30.minutes,
    private val configSource: RateLimiterConfigSource? = null,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default), // default
) : RateLimiterFactory {

    private val persistenceScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

    private val factory by lazy {
        AsyncFactoryBuilder.builder< RateLimiterConfig, RateLimiter<*>>(
            constructor = { name, config ->
                createResilience4jLimiter(
                    name = name,
                    config = config,
                    persistence = RateLimiterPersistenceContext(stateRegistry, persistenceScope),
                )
            },
            caching = AsyncFactoryCachingConfig(
                maxSize = maxLimiters,
                expireAfterAccess = expireAfterAccess,
                matchesConfig = { existing, newConfig -> existing.config == newConfig },
                recordStats = true,
            ),
            logging = AsyncFactoryLoggingConfig(enabled = true),
            metrics = null,
        )
    }

    override suspend fun create(
        name: String,
        config: RateLimiterConfig,
    ): RateLimiter<*> = factory.getOrCreate(name, config)

    override suspend fun create(name: String): RateLimiter<*> {
        val source = checkNotNull(configSource) {
            "No RateLimiterConfigSource configured. " +
                    "Either provide a config source or call create(name, config) with explicit configuration."
        }
        val config = checkNotNull(source.resolve(name)) {
            "No rate limiter configuration found for '$name'. " +
                    "Define it in your configuration source or call create(name, config) with explicit configuration."
        }
        return create(name, config)
    }
}
