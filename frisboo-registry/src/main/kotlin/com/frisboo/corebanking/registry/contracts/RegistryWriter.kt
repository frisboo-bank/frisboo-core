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
package com.frisboo.corebanking.registry.contracts

import arrow.core.Either
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryPutResult
import kotlin.time.Duration

/**
 * Write operations on a scoped registry.
 *
 * Implementations must be safe for concurrent use from coroutines.
 */
public interface RegistryWriter<K : Any, V : Any> {

    /**
     * Stores [value] under [key] with an optional time-to-live.
     *
     * @return [RegistryPutResult.Created] if the key was new, [RegistryPutResult.Updated] if overwritten,
     *         or [RegistryPutResult.Failed] with [RegistryError.InvalidTtl] if [ttl] is not positive
     *         (distributed implementations require [ttl] >= 1ms).
     */
    public suspend fun put(
        key: K,
        value: V,
        ttl: Duration? = null,
    ): Either<RegistryError, RegistryPutResult<V?>>

    /**
     * Returns the value for [key] if present; otherwise invokes [factory],
     * stores the result with an optional [ttl], and returns it.
     *
     * The result carries an explicit outcome signal ([RegistryGetOrPutResult.Created] vs
     * [RegistryGetOrPutResult.Found]) enabling accurate audit trails without racy pre-checks.
     *
     * Implementations may invoke [factory] speculatively under concurrent races;
     * callers must ensure [factory] is safe to call more than once.
     *
     * @return [RegistryGetOrPutResult.Created] if the factory was invoked and a new entry stored,
     *         [RegistryGetOrPutResult.Found] if an existing entry was returned, or
     *         [RegistryGetOrPutResult.Failed] with [RegistryError.InvalidTtl] if [ttl] is invalid.
     */
    public suspend fun getOrPut(
        key: K,
        ttl: Duration? = null,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>>

    /**
     * Removes the entry for [key].
     *
     * @return [RegistryEvictResult.Evicted] if removed, [RegistryEvictResult.NotFound] if absent.
     */
    public suspend fun evict(key: K): Either<RegistryError, RegistryEvictResult>
}
