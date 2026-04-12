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

import arrow.core.Either
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.contracts.RegistryMetrics
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.RegistryContainsResult
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryGetResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryPutResult
import com.frisboo.corebanking.registry.models.RegistrySetTtlResult
import com.frisboo.corebanking.registry.models.RegistrySizeResult
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
) : Registry<K, V> {

//    private val _hitCount = AtomicLong(0)
//    private val _missCount = AtomicLong(0)
//    private val _putCount = AtomicLong(0)
//    private val _evictionCount = AtomicLong(0)
//    private val _failureCount = AtomicLong(0)

//    override val hitCount: Long get() = _hitCount.get()
//    override val missCount: Long get() = _missCount.get()
//    override val putCount: Long get() = _putCount.get()
//    override val evictionCount: Long get() = _evictionCount.get()
//    override val failureCount: Long get() = _failureCount.get()

    override suspend fun get(key: K): Either<RegistryError, RegistryGetResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun contains(key: K): Either<RegistryError, RegistryContainsResult> {
        TODO("Not yet implemented")
    }

    override suspend fun size(): Either<RegistryError, RegistrySizeResult> {
        TODO("Not yet implemented")
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): Either<RegistryError, RegistryPutResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>> {
        TODO("Not yet implemented")
    }

    override suspend fun evict(key: K): Either<RegistryError, RegistryEvictResult> {
        TODO("Not yet implemented")
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<RegistryError, RegistryPage<K>> {
        TODO("Not yet implemented")
    }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<RegistryError, RegistrySetTtlResult> {
        TODO("Not yet implemented")
    }

//    override suspend fun get(key: K): Eit E V? {
//        val result = delegate.get(key)
//        if (result != null) _hitCount.incrementAndGet() else _missCount.incrementAndGet()
//        return result
//    }

//    override suspend fun contains(key: K): Boolean {
//        val result = delegate.contains(key)
//        if (result) _hitCount.incrementAndGet() else _missCount.incrementAndGet()
//        return result
//    }
//
//    override suspend fun put(
//        key: K,
//        value: V,
//        ttl: Duration?,
//    ): RegistryPutResult {
//        val result = delegate.put(key, value, ttl)
//        when (result) {
//            is RegistryPutResult.Created, is RegistryPutResult.Updated -> _putCount.incrementAndGet()
//            is RegistryPutResult.Failed -> _failureCount.incrementAndGet()
//        }
//        return result
//    }
//
//    override suspend fun getOrPut(
//        key: K,
//        ttl: Duration?,
//        factory: suspend () -> V,
//    ): RegistryGetOrPutResult<V> {
//        val result = delegate.getOrPut(key, ttl, factory)
//        when (result) {
//            is RegistryGetOrPutResult.Created -> {
//                _missCount.incrementAndGet()
//                _putCount.incrementAndGet()
//            }
//            is RegistryGetOrPutResult.Found -> _hitCount.incrementAndGet()
//            is RegistryGetOrPutResult.Failed -> _failureCount.incrementAndGet()
//        }
//        return result
//    }
//
//    override suspend fun evict(key: K): RegistryEvictResult {
//        val result = delegate.evict(key)
//        when (result) {
//            is RegistryEvictResult.Evicted -> _evictionCount.incrementAndGet()
//            is RegistryEvictResult.NotFound -> {}
//            is RegistryEvictResult.Failed -> _failureCount.incrementAndGet()
//        }
//        return result
//    }
//
//    override suspend fun setTTL(
//        key: K,
//        ttl: Duration,
//    ): RegistrySetTtlResult {
//        val result = delegate.setTTL(key, ttl)
//        when (result) {
//            is RegistrySetTtlResult.Applied -> {}
//            is RegistrySetTtlResult.NotFound -> {}
//            is RegistrySetTtlResult.Failed -> _failureCount.incrementAndGet()
//        }
//        return result
//    }
}
