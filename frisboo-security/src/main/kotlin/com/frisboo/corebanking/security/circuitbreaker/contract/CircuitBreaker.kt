package com.frisboo.corebanking.security.circuitbreaker.contract

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Core contract for circuit breaker implementations.
 * Provides fault tolerance by preventing cascading failures.
 */
public interface CircuitBreaker {

    public val name: String
    public val state: CircuitBreakerState

    public val failureRate: Float
    public val numberOfBufferedCalls: Int
    public val numberOfFailedCalls: Int
    public val numberOfSuccessfulCalls: Int

    public fun tryAcquirePermission(): Boolean
    public fun recordSuccess(duration: Duration)
    public fun recordFailure(duration: Duration, throwable: Throwable)

    public fun <T> execute(callable: Callable<T>): T
    public fun <T> execute(callable: Callable<T>, fallback: (Throwable?) -> T): T

    public fun <T> executeAsync(supplier: () -> CompletableFuture<T>): CompletableFuture<T>
    public fun <T> executeAsync(supplier: () -> CompletableFuture<T>, fallback: (Throwable?) -> T): CompletableFuture<T>

    public fun <T> executeAsync(callable: Callable<T>, executor: Executor): CompletableFuture<T> = executeAsync {
        CompletableFuture.supplyAsync(callable::call, executor)
    }
    public fun <T> executeAsync(
        callable: Callable<T>,
        executor: Executor,
        fallback: (Throwable?) -> T,
    ): CompletableFuture<T> {
        return executeAsync(
            {
                CompletableFuture.supplyAsync(callable::call, executor)
            },
            fallback,
        )
    }

    public suspend fun <T> executeSuspend(block: suspend () -> T): T
    public suspend fun <T> executeSuspend(block: suspend () -> T, fallback: suspend (Throwable?) -> T): T

    public fun trip()
    public fun reset()
}
