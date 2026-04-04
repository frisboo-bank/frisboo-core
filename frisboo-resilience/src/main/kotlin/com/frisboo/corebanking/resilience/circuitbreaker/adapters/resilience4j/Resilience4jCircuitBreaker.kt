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

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CallNotPermittedException
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerMetrics
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import io.github.resilience4j.circuitbreaker.CallNotPermittedException as R4jCallNotPermittedException

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
) : CircuitBreaker {

    override val metrics: CircuitBreakerMetrics = CircuitBreakerMetrics.from(delegate)

    override suspend fun <T> executeSuspend(block: suspend () -> T): T =
        executeWithPermission(
            block = block,
            onNotPermitted = {
                throw newCallNotPermittedException()
            },
            onFailure = { throw it },
        )

    override suspend fun <T> executeSuspend(
        block: suspend () -> T,
        fallback: suspend (Throwable) -> T,
    ): T =
        executeWithPermission(
            block = block,
            onNotPermitted = {
                fallback(newCallNotPermittedException())
            },
            onFailure = { fallback(it) },
        )

    override fun tryAcquirePermission(): Boolean = delegate.tryAcquirePermission()

    override fun trip(): Unit = delegate.transitionToOpenState()

    override fun reset(): Unit = delegate.reset()

    override fun recordSuccess(duration: Duration): Unit =
        delegate.onSuccess(duration.toJavaDuration().toNanos(), TimeUnit.NANOSECONDS)

    override fun recordFailure(
        duration: Duration,
        throwable: Throwable,
    ): Unit = delegate.onError(duration.toJavaDuration().toNanos(), TimeUnit.NANOSECONDS, throwable)

    private fun newCallNotPermittedException(): CallNotPermittedException =
        CallNotPermittedException(
            circuitBreakerName = delegate.name,
            cause = R4jCallNotPermittedException.createCallNotPermittedException(delegate),
        )

    /**
     * Shared permission-aware execution scaffold.
     *
     * Acquires a permit, times [block], and records the outcome atomically.
     * [CancellationException] releases the permit and propagates (structured concurrency).
     * [VirtualMachineError] releases the permit and propagates without recording.
     * Other JVM [Error]s are recorded and rethrown — never sent to fallbacks.
     *
     * @param block the protected operation.
     * @param onNotPermitted invoked when the circuit is open (no permit acquired).
     * @param onFailure invoked after [onError] is recorded; may throw or return a fallback.
     */
    private suspend fun <T> executeWithPermission(
        block: suspend () -> T,
        onNotPermitted: suspend () -> T,
        onFailure: suspend (Throwable) -> T,
    ): T {
        if (!delegate.tryAcquirePermission()) {
            return onNotPermitted()
        }
        val start = System.nanoTime()
        return try {
            val result = block()
            delegate.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS)
            result
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
            onFailure(e)
        }
    }
}
