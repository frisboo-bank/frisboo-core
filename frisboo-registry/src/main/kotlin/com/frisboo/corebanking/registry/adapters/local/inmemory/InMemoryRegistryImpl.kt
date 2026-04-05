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
package com.frisboo.corebanking.registry.adapters.local.inmemory

import com.frisboo.corebanking.registry.contracts.LocalRegistry
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.MAX_PAGE_SIZE
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import com.frisboo.corebanking.registry.models.buildPage
import com.frisboo.corebanking.registry.models.validateOptionalTtl
import com.frisboo.corebanking.registry.models.validateTtl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * HashMap-backed local registry; all operations synchronized via [Mutex], factory runs outside lock.
 */
public class InMemoryRegistryImpl<K : Any, V : Any>(
    public val scope: RegistryScope,
    private val clock: Clock = Clock.System,
) : LocalRegistry<K, V> {
    private val store = HashMap<K, InMemoryEntry<V>>()
    private val mutex = Mutex()

    override suspend fun get(key: K): V? =
        mutex.withLock {
            val now = clock.now()
            val entry = store[key] ?: return null
            if (isExpired(entry, now)) {
                store.remove(key)
                return null
            }
            entry.value
        }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> {
        validateOptionalTtl(ttl)?.let { return GetOrPutResult.Failed(it) }

        mutex.withLock {
            val now = clock.now()
            val existing = store[key]
            if (existing != null && !isExpired(existing, now)) {
                return GetOrPutResult.Found(existing.value)
            }
            if (existing != null) store.remove(key)
        }

        val value = factory()

        mutex.withLock {
            val now = clock.now()
            val raceWinner = store[key]
            if (raceWinner != null && !isExpired(raceWinner, now)) {
                return GetOrPutResult.Found(raceWinner.value)
            }
            store[key] = InMemoryEntry(value, ttl?.let { now + it })
        }
        return GetOrPutResult.Created(value)
    }

    override suspend fun contains(key: K): Boolean =
        mutex.withLock {
            val now = clock.now()
            val entry = store[key] ?: return false
            if (isExpired(entry, now)) {
                store.remove(key)
                return false
            }
            true
        }

    override suspend fun size(): Long =
        mutex.withLock {
            val now = clock.now()
            val iterator = store.entries.iterator()
            var count = 0L
            while (iterator.hasNext()) {
                val (_, entry) = iterator.next()
                if (isExpired(entry, now)) {
                    iterator.remove()
                } else {
                    count++
                }
            }
            count
        }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): PutResult =
        mutex.withLock {
            validateOptionalTtl(ttl)?.let { return PutResult.Failed(it) }
            val now = clock.now()
            val entry = InMemoryEntry(value, ttl?.let { now + it })
            val previous = store.put(key, entry)
            if (previous == null) PutResult.Created else PutResult.Updated
        }

    override suspend fun evict(key: K): EvictResult =
        mutex.withLock {
            val removed = store.remove(key)
            if (removed != null) EvictResult.Evicted else EvictResult.NotFound
        }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): RegistryPage<K> =
        mutex.withLock {
            val now = clock.now()
            val iterator = store.entries.iterator()
            val liveKeys = mutableListOf<K>()
            while (iterator.hasNext()) {
                val (key, entry) = iterator.next()
                if (isExpired(entry, now)) {
                    iterator.remove()
                } else {
                    liveKeys.add(key)
                }
            }
            liveKeys.sortBy { it.toString() }
            buildPage(liveKeys, cursor, limit, MAX_PAGE_SIZE)
        }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): SetTtlResult =
        mutex.withLock {
            validateTtl(ttl)?.let { return SetTtlResult.Failed(it) }
            val now = clock.now()
            val entry = store[key] ?: return SetTtlResult.KeyNotFound
            if (isExpired(entry, now)) {
                store.remove(key)
                return SetTtlResult.KeyNotFound
            }
            store[key] = entry.copy(expiresAt = now + ttl)
            SetTtlResult.Applied
        }

    override suspend fun cleanupExpired(): Int =
        mutex.withLock {
            val now = clock.now()
            var removed = 0
            val iterator = store.entries.iterator()
            while (iterator.hasNext()) {
                val (_, entry) = iterator.next()
                val expires = entry.expiresAt
                if (expires != null && now >= expires) {
                    iterator.remove()
                    removed++
                }
            }
            removed
        }

    private fun isExpired(
        entry: InMemoryEntry<V>,
        now: kotlin.time.Instant,
    ): Boolean {
        val expires = entry.expiresAt ?: return false
        return now >= expires
    }
}
