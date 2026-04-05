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

import com.frisboo.corebanking.core.factory.AsyncFactoryBuilder
import com.frisboo.corebanking.core.factory.models.AsyncFactoryCachingConfig
import com.frisboo.corebanking.core.factory.models.AsyncFactoryLoggingConfig
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j.createResilience4jBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerConfigSource
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerFactory
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerPersistenceContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Create and manage circuit breakers with automatic caching and optional external configuration source.
 *
 * @param stateRegistry Registry for persisting circuit breaker state across instances.
 * @param maxBreakers Maximum number of circuit breakers to cache before evicting old ones.
 * @param expireAfterAccess Duration after which an unused circuit breaker will be evicted from the cache.
 * @param configSource Optional source for resolving circuit breaker configurations by name.
 */
public class CircuitBreakerFactoryImpl(
    private val stateRegistry: Registry<String, String>,
    private val maxBreakers: Long = 10_000,
    private val expireAfterAccess: Duration = 30.minutes,
    private val configSource: CircuitBreakerConfigSource? = null,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default), // default
) : CircuitBreakerFactory {

    private val persistenceScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

    private val factory by lazy {
        AsyncFactoryBuilder.builder<CircuitBreakerConfig, CircuitBreaker<*>>(
            constructor = { name, config ->
                createResilience4jBreaker(
                    name = name,
                    config = config,
                    persistence = CircuitBreakerPersistenceContext(stateRegistry, persistenceScope),
                )
            },
            caching = AsyncFactoryCachingConfig(
                maxSize = maxBreakers,
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
        config: CircuitBreakerConfig,
    ): CircuitBreaker<*> = factory.getOrCreate(name, config)

    override suspend fun create(name: String): CircuitBreaker<*> {
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
}

