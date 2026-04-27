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
import com.frisboo.corebanking.statemanager.models.StateManagerPage
import com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult
import kotlin.time.Duration

/**
 * Administrative operations on a state manager.
 */
public interface StateManagerAdmin<K : Any, V : Any> {
    /**
     * Returns a cursor-based page of keys.
     *
     * @param cursor opaque cursor from a previous page, or `null` for the first page.
     * @param limit maximum number of keys to return.
     */
    public suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<StateManagerError, StateManagerPage<K>>

    /**
     * Updates the time-to-live for an existing [key].
     *
     * @return [StateManagerSetTtlResult.Applied] on success, [StateManagerSetTtlResult.NotFound] if absent,
     *         or [StateManagerSetTtlResult.Failed] with [StateManagerError.InvalidTtl] if [ttl] is not positive
     *         (distributed implementations require [ttl] >= 1ms).
     */
    public suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerSetTtlResult>
}
