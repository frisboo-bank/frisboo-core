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

public class StateManagerAuditingImpl<K : Any, V : Any>(
    private val delegate: StateManager<K, V>,
) : StateManager<K, V> {
    override suspend fun get(key: K): Either<StateManagerError, StateManagerGetResult<V?>> =
        either {
            return delegate.get(key)
        }

    override suspend fun contains(key: K): Either<StateManagerError, StateManagerContainsResult> =
        either {
            return delegate.contains(key)
        }

    override suspend fun size(): Either<StateManagerError, StateManagerSizeResult> =
        either {
            return delegate.size()
        }

    override suspend fun put(
        key: K,
        value: V,
    ): Either<StateManagerError, StateManagerPutResult<V?>> =
        either {
            return delegate.put(key, value)
        }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerPutResult<V?>> =
        either {
            return delegate.put(key, value, ttl)
        }

    override suspend fun getOrPut(
        key: K,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> =
        either {
            return delegate.getOrPut(key, factory)
        }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> =
        either {
            return delegate.getOrPut(key, ttl, factory)
        }

    override suspend fun evict(key: K): Either<StateManagerError, StateManagerEvictResult> =
        either {
            return delegate.evict(key)
        }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<StateManagerError, StateManagerPage<K>> =
        either {
            return delegate.keysPage(cursor, limit)
        }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerSetTtlResult> =
        either {
            return delegate.setTTL(key, ttl)
        }
}
