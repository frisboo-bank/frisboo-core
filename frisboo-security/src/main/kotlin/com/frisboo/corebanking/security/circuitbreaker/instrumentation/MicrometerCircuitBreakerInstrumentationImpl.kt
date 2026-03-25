package com.frisboo.corebanking.security.circuitbreaker.instrumentation

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerInstrumentation
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

public class MicrometerCircuitBreakerInstrumentationImpl(
    private val meterRegistry: MeterRegistry,
) : CircuitBreakerInstrumentation {

    // Registry-level metrics
    private val accessCounter: Counter = Counter.builder("circuit_breaker_access_total")
        .description("Total number of circuit breaker accesses")
        .register(meterRegistry)

    private val dynamicCreatedCounter: Counter = Counter.builder("circuit_breaker_dynamic_creation_total")
        .description("Total number of dynamic circuit breakers created")
        .register(meterRegistry)

    private val dynamicEvictedCounter: Counter = Counter.builder("circuit_breaker_dynamic_eviction_total")
        .description("Total number of dynamic circuit breakers evicted")
        .register(meterRegistry)

    private val cleanupRemovedCounter: Counter = Counter.builder("circuit_breaker_cleanup_removed_total")
        .description("Total number of stale dynamic breakers removed during cleanup")
        .register(meterRegistry)

    private val dynamicBreakerSize = AtomicInteger(0)

    init {
        meterRegistry.gauge("circuit_breaker_dynamic_size", dynamicBreakerSize) { it.get().toDouble() }
    }

    // Per-breaker metrics
    private val successCounters = ConcurrentHashMap<String, Counter>()
    private val successTimers = ConcurrentHashMap<String, Timer>()
    private val notPermittedCounters = ConcurrentHashMap<String, Counter>()
    private val notPermittedTimers = ConcurrentHashMap<String, Timer>()
    private val inFlightGauges = ConcurrentHashMap<String, AtomicInteger>()

    // Failure metrics – either per-exception or aggregated
    private val failureCounters =
        ConcurrentHashMap<String, Counter>() // aggregated when perExceptionMetricsEnabled == false
    private val failureTimers = ConcurrentHashMap<String, Timer>()

    private val transitionCounters = ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>()

    // Helper: get or create a counter using a supplier
    private fun getOrCreateCounter(
        map: ConcurrentHashMap<String, Counter>,
        name: String,
        supplier: () -> Counter,
    ): Counter {
        return map.computeIfAbsent(name) { supplier.invoke() }
    }

    // Helper: get or create a timer using a supplier
    private fun getOrCreateTimer(map: ConcurrentHashMap<String, Timer>, name: String, supplier: () -> Timer): Timer {
        return map.computeIfAbsent(name) { supplier.invoke() }
    }

    private fun getInFlightGauge(name: String): AtomicInteger = inFlightGauges.computeIfAbsent(name) {
        val gauge = AtomicInteger(0)
        meterRegistry.gauge("circuit_breaker_inflight", Tags.of("breaker", name), gauge) { it.get().toDouble() }
        gauge
    }

    override fun recordBreakerAccessed(name: String) {
        accessCounter.increment()
    }

    override fun recordDynamicBreakerCreated(name: String) {
        dynamicCreatedCounter.increment()
        dynamicBreakerSize.incrementAndGet()
    }

    override fun recordDynamicBreakerEvicted(name: String) {
        dynamicEvictedCounter.increment()
        dynamicBreakerSize.decrementAndGet()
        revokeBreakerMetrics(name)
    }

    override fun recordRegistryCleanup(removedCount: Int, remainingCount: Int) {
        cleanupRemovedCounter.increment(removedCount.toDouble())
        dynamicBreakerSize.set(remainingCount)
    }

    override fun recordCallStarted(name: String) {
        getInFlightGauge(name).incrementAndGet()
    }

    override fun recordCallSuccess(name: String, duration: Duration) {
        getOrCreateCounter(successCounters, name) {
            Counter.builder("circuit_breaker_call_success_total")
                .tag("breaker", name)
                .register(meterRegistry)
        }.increment()

        getOrCreateTimer(successTimers, name) {
            Timer.builder("circuit_breaker_call_success_duration_seconds")
                .tag("breaker", name)
                .register(meterRegistry)
        }.record(duration)

        getInFlightGauge(name).decrementAndGet()
    }

    override fun recordCallFailure(name: String, duration: Duration, throwable: Throwable) {
        getOrCreateCounter(failureCounters, name) {
            Counter.builder("circuit_breaker_call_failure_total")
                .tag("breaker", name)
                .register(meterRegistry)
        }.increment()

        getOrCreateTimer(failureTimers, name) {
            Timer.builder("circuit_breaker_call_failure_duration_seconds")
                .tag("breaker", name)
                .register(meterRegistry)
        }.record(duration)

        getInFlightGauge(name).decrementAndGet()
    }

    override fun recordCallNotPermitted(name: String, duration: Duration, state: CircuitBreakerState) {
        getOrCreateCounter(notPermittedCounters, name) {
            Counter.builder("circuit_breaker_call_not_permitted_total")
                .tag("breaker", name)
                .tag("state", state.name)
                .register(meterRegistry)
        }.increment()

        getOrCreateTimer(notPermittedTimers, name) {
            Timer.builder("circuit_breaker_call_not_permitted_duration_seconds")
                .tag("breaker", name)
                .tag("state", state.name)
                .register(meterRegistry)
        }.record(duration)

        getInFlightGauge(name).decrementAndGet()
    }

    override fun recordStateTransition(name: String, fromState: CircuitBreakerState, toState: CircuitBreakerState) {
        val transitionKey = "$fromState->$toState"
        val counterMap = transitionCounters.computeIfAbsent(name) { ConcurrentHashMap() }
        val counter = counterMap.computeIfAbsent(transitionKey) {
            Counter.builder("circuit_breaker_state_transitions_total")
                .tag("breaker", name)
                .tag("from_state", fromState.name)
                .tag("to_state", toState.name)
                .register(meterRegistry)
        }
        counter.increment()
    }

    override fun revokeBreakerMetrics(name: String) {
        // Remove all metrics for this breaker
        listOf(successCounters, successTimers, notPermittedCounters, notPermittedTimers, failureCounters, failureTimers)
            .forEach { map -> map.remove(name)?.let { meterRegistry.remove(it) } }

        transitionCounters.remove(name)?.values?.forEach { meterRegistry.remove(it) }

        meterRegistry.find("circuit_breaker_inflight").tag("breaker", name).meter()?.let { meterRegistry.remove(it) }
        inFlightGauges.remove(name)
    }
}
