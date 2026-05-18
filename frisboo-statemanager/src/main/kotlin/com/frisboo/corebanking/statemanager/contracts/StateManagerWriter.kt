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
package com.frisboo.corebanking.statemanager.contracts

import arrow.core.Either
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.StateManagerEvictResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import kotlin.time.Duration

/**
 * Write operations on a scoped statemanager.
 *
 * Implementations must be safe for concurrent use from coroutines.
 */
public interface StateManagerWriter<K : Any, V : Any> {
    public suspend fun put(
        key: K,
        value: V,
    ): Either<StateManagerError, StateManagerPutResult<V?>>

    public suspend fun put(
        key: K,
        value: V,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerPutResult<V?>>

    public suspend fun getOrPut(
        key: K,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>>

    public suspend fun getOrPut(
        key: K,
        ttl: Duration,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>>

    /**
     * Removes the entry for [key].
     *
     * @return [StateManagerEvictResult.Evicted] if removed, [StateManagerEvictResult.NotFound] if absent.
     */
    public suspend fun evict(key: K): Either<StateManagerError, StateManagerEvictResult>
}
