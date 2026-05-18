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
package com.frisboo.corebanking.statemanager.decorators

import arrow.core.Either
import arrow.core.raise.either
import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.contracts.StateManagerResilienceExecutor
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.StateManagerContainsResult
import com.frisboo.corebanking.statemanager.models.StateManagerEvictResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetResult
import com.frisboo.corebanking.statemanager.models.StateManagerPage
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult
import com.frisboo.corebanking.statemanager.models.StateManagerSizeResult
import kotlin.time.Duration

public class StateManagerResilientImpl<K : Any, V : Any>(
    private val primary: StateManager<K, V>,
    private val fallback: StateManager<K, V>,
    private val resilientExecutor: StateManagerResilienceExecutor,
) : StateManager<K, V> {
    override suspend fun get(key: K): Either<StateManagerError, StateManagerGetResult<V?>> =
        resilient(
            primaryOption = { primary.get(key) },
            fallbackOption = { fallback.get(key) },
        )

    override suspend fun contains(key: K): Either<StateManagerError, StateManagerContainsResult> =
        resilient(
            primaryOption = { primary.contains(key) },
            fallbackOption = { fallback.contains(key) },
        )

    override suspend fun size(): Either<StateManagerError, StateManagerSizeResult> =
        resilient(
            primaryOption = { primary.size() },
            fallbackOption = { fallback.size() },
        )

    override suspend fun put(
        key: K,
        value: V,
    ): Either<StateManagerError, StateManagerPutResult<V?>> =
        resilient(
            primaryOption = { primary.put(key, value) },
            fallbackOption = { fallback.put(key, value) },
        )

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerPutResult<V?>> =
        resilient(
            primaryOption = { primary.put(key, value, ttl) },
            fallbackOption = { fallback.put(key, value, ttl) },
        )

    override suspend fun getOrPut(
        key: K,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> {
        var factoryResult: V? = null
        val cachedFactory: suspend () -> V = {
            factoryResult ?: factory().also { factoryResult = it }
        }

        return resilient(
            primaryOption = { primary.getOrPut(key, cachedFactory) },
            fallbackOption = { fallback.getOrPut(key, cachedFactory) },
        )
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> {
        var factoryResult: V? = null
        val cachedFactory: suspend () -> V = {
            factoryResult ?: factory().also { factoryResult = it }
        }

        return resilient(
            primaryOption = { primary.getOrPut(key, ttl, cachedFactory) },
            fallbackOption = { fallback.getOrPut(key, ttl, cachedFactory) },
        )
    }

    override suspend fun evict(key: K): Either<StateManagerError, StateManagerEvictResult> =
        resilient(
            primaryOption = { primary.evict(key) },
            fallbackOption = { fallback.evict(key) },
        )

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<StateManagerError, StateManagerPage<K>> =
        resilient(
            primaryOption = { primary.keysPage(cursor, limit) },
            fallbackOption = { fallback.keysPage(cursor, limit) },
        )

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerSetTtlResult> =
        resilient(
            primaryOption = { primary.setTTL(key, ttl) },
            fallbackOption = { fallback.setTTL(key, ttl) },
        )

    private suspend fun <T> resilient(
        primaryOption: suspend () -> T,
        fallbackOption: suspend () -> T,
    ): T = resilientExecutor.execute(primaryOption, fallbackOption)

//    override suspend fun get(key: K): V? =
//        resilient(
//            primaryOption = { primary.get(key) },
//            fallbackOption = { fallback.get(key) },
//        )
//
//    override suspend fun getOrPut(
//        key: K,
//        ttl: Duration?,
//        factory: suspend () -> V,
//    ): StateManagerGetOrPutResult<V> {
//        var factoryResult: V? = null
//        val cachedFactory: suspend () -> V = {
//            factoryResult ?: factory().also { factoryResult = it }
//        }
//        return resilient(
//            primaryOption = { primary.getOrPut(key, ttl, cachedFactory) },
//            fallbackOption = { fallback.getOrPut(key, ttl, cachedFactory) },
//        )
//    }
//
//    override suspend fun contains(key: K): Boolean =
//        resilient(
//            primaryOption = { primary.contains(key) },
//            fallbackOption = { fallback.contains(key) },
//        )
//
//    override suspend fun size(): Long =
//        resilient(
//            primaryOption = { primary.size() },
//            fallbackOption = { fallback.size() },
//        )
//
//    override suspend fun put(
//        key: K,
//        value: V,
//        ttl: Duration?,
//    ): StateManagerPutResult =
//        resilient(
//            primaryOption = { primary.put(key, value, ttl) },
//            fallbackOption = { fallback.put(key, value, ttl) },
//        )
//
//    override suspend fun evict(key: K): StateManagerEvictResult =
//        resilient(
//            primaryOption = { primary.evict(key) },
//            fallbackOption = { fallback.evict(key) },
//        )
//
//    override suspend fun keysPage(
//        cursor: String?,
//        limit: Int,
//    ): StateManagerPage<K> =
//        resilient(
//            primaryOption = { primary.keysPage(cursor, limit) },
//            fallbackOption = { fallback.keysPage(cursor, limit) },
//        )
//
//    override suspend fun setTTL(
//        key: K,
//        ttl: Duration,
//    ): StateManagerSetTtlResult =
//        resilient(
//            primaryOption = { primary.setTTL(key, ttl) },
//            fallbackOption = { fallback.setTTL(key, ttl) },
//        )
}
