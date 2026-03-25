package com.frisboo.corebanking.security.circuitbreaker.registry

import com.frisboo.corebanking.security.circuitbreaker.config.CircuitBreakerConfigResolver
import com.frisboo.corebanking.security.circuitbreaker.config.CircuitBreakerProperties.CircuitBreakerInstanceConfig
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerFactory
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerInstrumentation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerRegistry
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerEntry
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerRegistryConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerRegistryState
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerSnapshot
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

/**
 * Registry for managing circuit breakers by name.
 */
public class CircuitBreakerRegistryImpl(
    private val factory: CircuitBreakerFactory,
    private val registryConfig: CircuitBreakerRegistryConfig,
    private val globalConfig: CircuitBreakerConfig,
    private val instanceConfigs: Map<String, CircuitBreakerConfig>,
    private val clock: Clock = Clock.systemUTC(),
    private val instrumentation: CircuitBreakerInstrumentation,
) : CircuitBreakerRegistry, AutoCloseable {

    private val coreBreakers = ConcurrentHashMap<String, CircuitBreaker>()
    private val dynamicBreakers = ConcurrentHashMap<String, CircuitBreakerEntry>()

    private val state = AtomicReference(CircuitBreakerRegistryState.IDLE)
    private var cleanupExecutor: ScheduledExecutorService? = null

    private val lock = ReentrantLock()

    private companion object {
        private const val CLEANUP_THREAD_NAME = "frisboo-circuit-breaker-cleanup"
        private const val EVICTION_SAMPLE_SIZE = 200
        private const val MAX_SNAPSHOT_PAGE_SIZE = 1_000
        private const val CLEANUP_MAX_SCAN_SIZE = 50_000

        private val logger = KotlinLogging.logger { }
    }

    init {
        instanceConfigs.forEach { (name, config) ->
            coreBreakers[name] = factory.create(name, config)
        }
    }

    public override fun start() {
        if (!state.compareAndSet(CircuitBreakerRegistryState.IDLE, CircuitBreakerRegistryState.STARTED)) {
            return
        }
        logger.info { "Starting CircuitBreakerRegistry with ${coreBreakers.size} breakers" }
        startCleanupSchedule()
    }

    override fun getBreakersSnapshot(
        page: Int,
        size: Int,
    ): Map<String, CircuitBreakerSnapshot> {
        require(page >= 0) { "Page index must be non-negative" }
        require(size in 1..MAX_SNAPSHOT_PAGE_SIZE) {
            "Page size must be between 1 and $MAX_SNAPSHOT_PAGE_SIZE"
        }
        val skip = page.toLong() * size
        require(skip <= Int.MAX_VALUE) { "Page index and size too large" }

        val coreSequence = coreBreakers.asSequence().map { (name, breaker) -> name to breaker }
        val dynamicSequence = dynamicBreakers.asSequence().map { (name, entry) -> name to entry.breaker }

        return (coreSequence + dynamicSequence)
            .drop(skip.toInt())
            .take(size)
            .associate { (name, entry) ->
                name to CircuitBreakerSnapshot.from(entry)
            }
    }

    override fun close() {
        if (state.getAndSet(CircuitBreakerRegistryState.CLOSED) == CircuitBreakerRegistryState.CLOSED) {
            return
        }
        logger.info { "Closing CircuitBreakerRegistry" }
        stopCleanupSchedule()
        lock.withLock {
            coreBreakers.clear()
            dynamicBreakers.clear()
        }
    }

    public override fun get(name: String): CircuitBreaker {
        requireValidName(name)
        check(state.get() != CircuitBreakerRegistryState.CLOSED) { "Registry is closed" }

        coreBreakers[name]?.let {
            instrumentation.recordBreakerAccessed(name)
            return it
        }

        val entry = dynamicBreakers[name]
            ?: throw NoSuchElementException("No circuit breaker found with name '$name'")

        entry.lastAccessedAt.set(clock.millis())
        instrumentation.recordBreakerAccessed(name)
        return entry.breaker
    }

    public override fun getOrCreate(name: String, config: CircuitBreakerInstanceConfig): CircuitBreaker {
        requireValidName(name)
        check(state.get() != CircuitBreakerRegistryState.CLOSED) { "Registry is closed" }

        coreBreakers[name]?.let {
            instrumentation.recordBreakerAccessed(name)
            return it
        }

        dynamicBreakers[name]?.let { entry ->
            entry.lastAccessedAt.set(clock.millis())
            instrumentation.recordBreakerAccessed(name)
            return entry.breaker
        }

        lock.withLock {
            dynamicBreakers[name]?.let { entry ->
                entry.lastAccessedAt.set(clock.millis())
                instrumentation.recordBreakerAccessed(name)
                return entry.breaker
            }

            while (dynamicBreakers.size >= registryConfig.maxDynamicBreakers) {
                val oldestEntry = findOldestEntry() ?: break
                dynamicBreakers.remove(oldestEntry.breaker.name)
                instrumentation.recordDynamicBreakerEvicted(oldestEntry.breaker.name)
            }

            val newEntry = createEntry(name, config)
            instrumentation.recordDynamicBreakerCreated(name)
            dynamicBreakers[name] = newEntry
            instrumentation.recordBreakerAccessed(name)
            return newEntry.breaker
        }
    }

    private fun findOldestEntry(): CircuitBreakerEntry? {
        if (dynamicBreakers.isEmpty()) return null
        val sampleSize = min(EVICTION_SAMPLE_SIZE, dynamicBreakers.size)
        var oldest: CircuitBreakerEntry? = null

        for ((count, entry) in dynamicBreakers.values.withIndex()) {
            if (count >= sampleSize) break
            if (oldest == null || entry.lastAccessedAt.get() < oldest.lastAccessedAt.get()) {
                oldest = entry
            }
        }
        return oldest
    }

    private fun createEntry(
        name: String,
        config: CircuitBreakerInstanceConfig,
    ): CircuitBreakerEntry {
        requireValidName(name)

        val breakerConfig = CircuitBreakerConfigResolver.resolve(globalConfig, config)
        val breaker = factory.create(name, breakerConfig)

        return CircuitBreakerEntry(
            breaker = breaker,
            lastAccessedAt = AtomicLong(clock.millis()),
        ).also {
            logger.info { "Created circuit breaker '$name'" }
        }
    }

    private fun cleanup() {
        if (state.get() != CircuitBreakerRegistryState.STARTED) return

        try {
            val cutoff = clock.millis() - registryConfig.dynamicBreakerTTL.toMillis()
            var removedCount = 0
            var scannedCount = 0
            val startNanos = System.nanoTime()
            val budgetNanos = registryConfig.cleanupTimeBudgetMs * 1_000_000L

            val iterator = dynamicBreakers.entries.iterator()
            while (iterator.hasNext() &&
                scannedCount < CLEANUP_MAX_SCAN_SIZE &&
                (System.nanoTime() - startNanos) < budgetNanos
            ) {
                val entry = iterator.next()
                scannedCount++
                if (entry.value.lastAccessedAt.get() >= cutoff) continue

                iterator.remove()
                instrumentation.recordDynamicBreakerEvicted(entry.key)
                removedCount++
            }

            if (removedCount > 0) {
                instrumentation.recordRegistryCleanup(removedCount, dynamicBreakers.size)
                logger.info {
                    "Removed $removedCount stale dynamic circuit breaker(s) " +
                            "after scanning $scannedCount entries"
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error during circuit breaker cleanup" }
        }
    }

    private fun startCleanupSchedule() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, CLEANUP_THREAD_NAME).apply { isDaemon = true }
        }.also { ex ->
            ex.scheduleAtFixedRate(
                { cleanup() },
                registryConfig.cleanupInitialDelayMinutes,
                registryConfig.cleanupPeriodMinutes,
                TimeUnit.MINUTES,
            )
        }
    }

    private fun stopCleanupSchedule() {
        cleanupExecutor?.shutdown()
        try {
            if (cleanupExecutor?.awaitTermination(registryConfig.shutdownTimeoutSeconds, TimeUnit.SECONDS) != true) {
                cleanupExecutor?.shutdownNow()
            }
        } catch (_: InterruptedException) {
            cleanupExecutor?.shutdownNow()
            Thread.currentThread().interrupt()
        }
        cleanupExecutor = null
    }

    private fun requireValidName(name: String) {
        require(name.isNotBlank()) { "Circuit breaker name must not be blank" }
    }
}

