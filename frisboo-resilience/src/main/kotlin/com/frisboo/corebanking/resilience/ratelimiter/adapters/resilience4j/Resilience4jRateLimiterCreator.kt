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
package com.frisboo.corebanking.resilience.ratelimiter.adapters.resilience4j

import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterPersistenceContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import io.github.resilience4j.ratelimiter.RateLimiter as R4jRateLimiter

private val logger = KotlinLogging.logger {}

internal suspend fun createResilience4jLimiter(
    name: String,
    config: RateLimiterConfig,
    persistence: RateLimiterPersistenceContext,
): Resilience4jRateLimiter {
    val r4jConfig = config.toResilience4jConfig()
    val r4jLimiter = R4jRateLimiter.of(name, r4jConfig)
    val (stateRegistry, coroutineScope) = persistence

    val configValue = "${config.limitForPeriod}:${config.limitRefreshPeriod}:${config.timeoutDuration}"

    r4jLimiter.eventPublisher.onSuccess {
        coroutineScope.launch {
            try {
                stateRegistry.put(name, configValue)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "Failed to persist rate limiter config for '$name'" }
            }
        }
    }

    try {
        stateRegistry.put(name, configValue)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn(e) { "Failed to persist initial rate limiter config for '$name'" }
    }

    return Resilience4jRateLimiter(r4jLimiter, config)
}
