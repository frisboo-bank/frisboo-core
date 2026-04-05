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
package com.frisboo.corebanking.resilience.circuitbreaker.autoconfigure

import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.resilience.circuitbreaker.CircuitBreakerFactoryImpl
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerFactory
import com.frisboo.corebanking.resilience.ratelimiter.RateLimiterFactoryImpl
import com.frisboo.corebanking.resilience.ratelimiter.autoconfigure.PropertiesRateLimiterConfigSource
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnProperty(prefix = "frisboo.corebanking.resilience", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(ResilienceProperties::class)
public open class ResilienceAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "frisboo.corebanking.resilience.circuit-breaker",
        name = ["enabled"],
        havingValue = "true",
    )
    @ConditionalOnBean(name = ["circuitBreakerStateRegistry"])
    @ConditionalOnMissingBean(CircuitBreakerFactory::class)
    public open fun circuitBreakerFactory(
        @Qualifier("circuitBreakerStateRegistry") stateRegistry: Registry<String, String>,
        properties: ResilienceProperties,
    ): CircuitBreakerFactory =
        CircuitBreakerFactoryImpl(
            stateRegistry = stateRegistry,
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            maxBreakers = properties.circuitBreaker.maxBreakers,
            configSource = PropertiesCircuitBreakerConfigSource(properties),
        )

    @Bean
    @ConditionalOnProperty(
        prefix = "frisboo.corebanking.resilience.rate-limiter",
        name = ["enabled"],
        havingValue = "true",
    )
    @ConditionalOnBean(name = ["rateLimiterStateRegistry"])
    @ConditionalOnMissingBean(RateLimiterFactory::class)
    public open fun rateLimiterFactory(
        @Qualifier("rateLimiterStateRegistry") stateRegistry: Registry<String, String>,
        properties: ResilienceProperties,
    ): RateLimiterFactory =
        RateLimiterFactoryImpl(
            stateRegistry = stateRegistry,
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            maxLimiters = properties.rateLimiter.maxLimiters,
            configSource = PropertiesRateLimiterConfigSource(properties),
        )
}
