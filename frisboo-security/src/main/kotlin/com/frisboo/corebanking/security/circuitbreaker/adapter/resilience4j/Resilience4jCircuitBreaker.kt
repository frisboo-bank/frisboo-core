package com.frisboo.corebanking.security.circuitbreaker.adapter.resilience4j

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerException.CircuitBreakerOpenException
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import io.github.resilience4j.circuitbreaker.CircuitBreaker as VendorCircuitBreaker

/**
 * Resilience4j-based circuit breaker implementation.
 */
internal class Resilience4jCircuitBreaker(
    override val name: String,
    private val delegate: VendorCircuitBreaker,
) : CircuitBreaker {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private fun VendorCircuitBreaker.State.toModel(): CircuitBreakerState = when (delegate.state) {
        VendorCircuitBreaker.State.CLOSED -> CircuitBreakerState.CLOSED
        VendorCircuitBreaker.State.OPEN -> CircuitBreakerState.OPEN
        VendorCircuitBreaker.State.HALF_OPEN -> CircuitBreakerState.HALF_OPEN
        VendorCircuitBreaker.State.FORCED_OPEN -> CircuitBreakerState.FORCED_OPEN
        VendorCircuitBreaker.State.DISABLED -> CircuitBreakerState.DISABLED
        else -> CircuitBreakerState.OPEN
    }

    init {
        require(name.isNotBlank()) { "Circuit breaker name must not be blank" }

        delegate.eventPublisher.onStateTransition { event ->
            val fromState = event.stateTransition.fromState
            val toState = event.stateTransition.toState

            logger.info {
                "Circuit breaker '$name' transitioned from $fromState to $toState"
            }
        }
        delegate.eventPublisher.onError { event ->
            logger.debug { "Circuit breaker '$name' recorded error: ${event.throwable.javaClass.simpleName}" }
        }
    }

    override val state: CircuitBreakerState
        get() = delegate.state.toModel()

    override val failureRate: Float get() = delegate.metrics.failureRate
    override val numberOfBufferedCalls: Int get() = delegate.metrics.numberOfBufferedCalls
    override val numberOfFailedCalls: Int get() = delegate.metrics.numberOfFailedCalls
    override val numberOfSuccessfulCalls: Int get() = delegate.metrics.numberOfSuccessfulCalls

    override fun tryAcquirePermission(): Boolean = delegate.tryAcquirePermission()

    override fun recordSuccess(duration: Duration) {
        require(!duration.isNegative) { "Duration cannot be negative" }
        delegate.onSuccess(duration.toNanos(), TimeUnit.NANOSECONDS)
    }

    override fun recordFailure(duration: Duration, throwable: Throwable) {
        require(!duration.isNegative) { "Duration cannot be negative" }
        delegate.onError(duration.toNanos(), TimeUnit.NANOSECONDS, throwable)
    }

    override fun <T> execute(callable: Callable<T>): T {
        return try {
            delegate.executeCallable(callable)
        } catch (e: CallNotPermittedException) {
            throw CircuitBreakerOpenException(
                "Circuit breaker '$name' is ${state.name} and does not permit execution",
                name,
                state,
                e,
            )
        }
    }

    override fun <T> executeAsync(
        supplier: () -> CompletableFuture<T>,
    ): CompletableFuture<T> {
        val completionStage = delegate.executeCompletionStage(supplier)
        val future = CompletableFuture<T>()

        completionStage.whenComplete { result, error ->
            when (error) {
                null -> future.complete(result)
                is CallNotPermittedException -> future.completeExceptionally(
                    CircuitBreakerOpenException(
                        "Circuit breaker '$name' is ${state.name} and does not permit execution",
                        name,
                        state,
                        error,
                    ),
                )

                else -> future.completeExceptionally(error)
            }
        }

        return future
    }

    override suspend fun <T> executeSuspend(block: suspend () -> T): T {
        return try {
            delegate.executeSuspendFunction(block)
        } catch (e: CallNotPermittedException) {
            throw CircuitBreakerOpenException(
                "Circuit breaker '$name' is ${state.name} and does not permit execution",
                name,
                state,
                e,
            )
        }
    }

    override fun trip() {
        logger.warn { "Circuit breaker '$name' manually tripped to OPEN" }
        delegate.transitionToOpenState()
    }

    override fun reset() {
        logger.info { "Circuit breaker '$name' manually reset to CLOSED" }
        delegate.transitionToClosedState()
    }
}
