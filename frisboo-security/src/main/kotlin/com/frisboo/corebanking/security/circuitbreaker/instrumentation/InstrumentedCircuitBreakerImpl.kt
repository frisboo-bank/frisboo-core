package com.frisboo.corebanking.security.circuitbreaker.instrumentation

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerInstrumentation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservationFactory
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerException.CircuitBreakerOpenException
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

internal class InstrumentedCircuitBreakerImpl(
    private val delegate: CircuitBreaker,
    private val instrumentation: CircuitBreakerInstrumentation,
    private val observationFactory: CircuitBreakerObservationFactory,
) : CircuitBreaker {

    override val name: String get() = delegate.name
    override val state: CircuitBreakerState get() = delegate.state
    override val failureRate: Float get() = delegate.failureRate
    override val numberOfBufferedCalls: Int get() = delegate.numberOfBufferedCalls
    override val numberOfFailedCalls: Int get() = delegate.numberOfFailedCalls
    override val numberOfSuccessfulCalls: Int get() = delegate.numberOfSuccessfulCalls

    override fun tryAcquirePermission(): Boolean = delegate.tryAcquirePermission()
    override fun recordSuccess(duration: Duration) = delegate.recordSuccess(duration)
    override fun recordFailure(duration: Duration, throwable: Throwable) = delegate.recordFailure(duration, throwable)

    override fun <T> execute(callable: Callable<T>): T = execute(callable) { throw it!! }
    override fun <T> executeAsync(supplier: () -> CompletableFuture<T>): CompletableFuture<T> =
        executeAsync(supplier) { throw it!! }

    override fun <T> executeAsync(callable: Callable<T>, executor: Executor): CompletableFuture<T> =
        executeAsync(callable, executor) { throw it!! }

    override suspend fun <T> executeSuspend(block: suspend () -> T): T = executeSuspend(block) { throw it!! }

    override fun <T> execute(callable: Callable<T>, fallback: (Throwable?) -> T): T {
        instrumentation.recordCallStarted(name)

        return instrumentCall(
            observation = observationFactory.createObservation(name),
            startTime = System.nanoTime(),
            onSuccess = { delegate.execute(callable) },
            onFailure = { fallback(it) },
            onNotPermitted = { fallback(null) },
        )
    }

    override fun <T> executeAsync(
        supplier: () -> CompletableFuture<T>,
        fallback: (Throwable?) -> T,
    ): CompletableFuture<T> {
        val observation = observationFactory.createObservation(name)
        val startTime = System.nanoTime()
        observation.start()
        instrumentation.recordCallStarted(name)

        return try {
            val future = delegate.executeAsync(supplier)
            future.whenComplete { result, error ->
                when (error) {
                    null -> recordSuccess(observation, startTime)
                    is CircuitBreakerOpenException -> recordNotPermitted(observation, startTime)
                    else -> recordFailure(observation, startTime, error)
                }
                observation.close()
            }
            future
        } catch (e: Exception) {
            // Handle synchronous exception from delegate (unlikely but safe)
            when (e) {
                is CircuitBreakerOpenException -> recordNotPermitted(observation, startTime)
                else -> recordFailure(observation, startTime, e)
            }
            observation.close()
            CompletableFuture.completedFuture(fallback(e))
        }
    }

    override suspend fun <T> executeSuspend(
        block: suspend () -> T,
        fallback: suspend (Throwable?) -> T,
    ): T {
        instrumentation.recordCallStarted(name)

        return instrumentCall(
            observation = observationFactory.createObservation(name),
            startTime = System.nanoTime(),
            onSuccess = { delegate.executeSuspend(block) },
            onFailure = { fallback(it) },
            onNotPermitted = { fallback(null) },
        )
    }

    override fun trip() = delegate.trip()
    override fun reset() = delegate.reset()

    private inline fun <T> instrumentCall(
        observation: CircuitBreakerObservation,
        startTime: Long,
        onSuccess: () -> T,
        onFailure: (Throwable) -> T,
        onNotPermitted: () -> T,
    ): T {
        observation.start()

        return try {
            val result = onSuccess()
            recordSuccess(observation, startTime)
            result
        } catch (e: CircuitBreakerOpenException) {
            recordNotPermitted(observation, startTime)
            onNotPermitted()
        } catch (e: Exception) {
            recordFailure(observation, startTime, e)
            onFailure(e)
        } finally {
            observation.close()
        }
    }

    private fun recordSuccess(
        observation: CircuitBreakerObservation,
        startTime: Long,
    ) {
        val duration = Duration.ofNanos(System.nanoTime() - startTime)
        observation.recordSuccess(duration)
        instrumentation.recordCallSuccess(name, duration)
    }

    private fun recordNotPermitted(
        observation: CircuitBreakerObservation,
        startTime: Long,
    ) {
        val duration = Duration.ofNanos(System.nanoTime() - startTime)
        observation.recordNotPermitted(duration, state)
        instrumentation.recordCallNotPermitted(name, duration, state)
    }

    private fun recordFailure(
        observation: CircuitBreakerObservation,
        startTime: Long,
        throwable: Throwable,
    ) {
        val duration = Duration.ofNanos(System.nanoTime() - startTime)
        observation.recordFailure(duration, throwable)
        instrumentation.recordCallFailure(name, duration, throwable)
    }
}
