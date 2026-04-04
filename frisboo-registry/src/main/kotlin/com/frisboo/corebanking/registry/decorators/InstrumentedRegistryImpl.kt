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
package com.frisboo.corebanking.registry.decorators

import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.contracts.RegistryMetrics
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.SetTtlResult
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

/**
 * Metrics decorator: tracks hit/miss/put/eviction/failure counters for a [Registry].
 *
 * Counters are lock-free ([AtomicLong]) and safe for concurrent coroutine use.
 * Read-only counters via [RegistryMetrics] — suitable for Micrometer gauge binding
 * or periodic scraping without affecting registry throughput.
 *
 * Delegates all [Registry] operations unchanged; only the counting behaviour is added.
 */
public class InstrumentedRegistryImpl<K : Any, V : Any>(
    private val delegate: Registry<K, V>,
) : Registry<K, V> by delegate, RegistryMetrics {
    private val _hitCount = AtomicLong(0)
    private val _missCount = AtomicLong(0)
    private val _putCount = AtomicLong(0)
    private val _evictionCount = AtomicLong(0)
    private val _failureCount = AtomicLong(0)

    override val hitCount: Long get() = _hitCount.get()
    override val missCount: Long get() = _missCount.get()
    override val putCount: Long get() = _putCount.get()
    override val evictionCount: Long get() = _evictionCount.get()
    override val failureCount: Long get() = _failureCount.get()

    override suspend fun get(key: K): V? {
        val result = delegate.get(key)
        if (result != null) _hitCount.incrementAndGet() else _missCount.incrementAndGet()
        return result
    }

    override suspend fun contains(key: K): Boolean {
        val result = delegate.contains(key)
        if (result) _hitCount.incrementAndGet() else _missCount.incrementAndGet()
        return result
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): PutResult {
        val result = delegate.put(key, value, ttl)
        when (result) {
            is PutResult.Created, is PutResult.Updated -> _putCount.incrementAndGet()
            is PutResult.Failed -> _failureCount.incrementAndGet()
        }
        return result
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> {
        val result = delegate.getOrPut(key, ttl, factory)
        when (result) {
            is GetOrPutResult.Created -> {
                _missCount.incrementAndGet()
                _putCount.incrementAndGet()
            }
            is GetOrPutResult.Found -> _hitCount.incrementAndGet()
            is GetOrPutResult.Failed -> _failureCount.incrementAndGet()
        }
        return result
    }

    override suspend fun evict(key: K): EvictResult {
        val result = delegate.evict(key)
        when (result) {
            is EvictResult.Evicted -> _evictionCount.incrementAndGet()
            is EvictResult.NotFound -> {}
            is EvictResult.Failed -> _failureCount.incrementAndGet()
        }
        return result
    }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): SetTtlResult {
        val result = delegate.setTTL(key, ttl)
        when (result) {
            is SetTtlResult.Applied -> {}
            is SetTtlResult.KeyNotFound -> {}
            is SetTtlResult.Failed -> _failureCount.incrementAndGet()
        }
        return result
    }
}
