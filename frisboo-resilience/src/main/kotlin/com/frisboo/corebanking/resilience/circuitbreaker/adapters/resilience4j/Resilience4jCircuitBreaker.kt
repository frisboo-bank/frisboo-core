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
package com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerMetrics
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerResult
import com.frisboo.corebanking.resilience.circuitbreaker.errors.CircuitBreakerError
import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as R4jConfig

/**
 * Resilience4j-backed [CircuitBreaker] implementation.
 *
 * Thread-safety is delegated to the underlying [R4jCircuitBreaker] which is
 * inherently thread-safe. Permission lifecycle is atomic: every
 * [tryAcquirePermission] is balanced by exactly one [onSuccess], [onError],
 * or [releasePermission] call.
 */
internal class Resilience4jCircuitBreaker(
    private val delegate: R4jCircuitBreaker,
    override val config: CircuitBreakerConfig,
) : CircuitBreaker<R4jConfig> {
    override val metrics: CircuitBreakerMetrics = CircuitBreakerMetrics.from(delegate)

    override val internalConfig: R4jConfig get() = delegate.circuitBreakerConfig

    override suspend fun <T> executeSuspend(block: suspend () -> T): CircuitBreakerResult<T> {
        if (!delegate.tryAcquirePermission()) {
            return CircuitBreakerResult.Rejected(
                CircuitBreakerError.CallNotPermitted(
                    message = "CircuitBreaker '${delegate.name}' is OPEN and does not permit further calls",
                ),
            )
        }

        val start = System.nanoTime()
        return try {
            val result = block()
            delegate.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS)
            CircuitBreakerResult.Success(result)
        } catch (e: CancellationException) {
            delegate.releasePermission()
            throw e
        } catch (e: VirtualMachineError) {
            delegate.releasePermission()
            throw e
        } catch (e: Error) {
            delegate.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, e)
            throw e
        } catch (e: Throwable) {
            delegate.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, e)
            CircuitBreakerResult.Failure(e)
        }
    }

    override fun tryAcquirePermission(): Boolean = delegate.tryAcquirePermission()

    override fun trip(): Unit = delegate.transitionToOpenState()

    override fun reset(): Unit = delegate.reset()

    override fun recordSuccess(duration: Duration): Unit =
        delegate.onSuccess(duration.toJavaDuration().toNanos(), TimeUnit.NANOSECONDS)

    override fun recordFailure(
        duration: Duration,
        throwable: Throwable,
    ): Unit = delegate.onError(duration.toJavaDuration().toNanos(), TimeUnit.NANOSECONDS, throwable)
}
