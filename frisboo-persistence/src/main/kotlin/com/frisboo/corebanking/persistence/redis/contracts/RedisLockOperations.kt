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
package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult

public interface RedisLockOperations<K : Any, V : Any> {
    public suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>>

    public suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>>

    public suspend fun acquireLock(key: K, ttlMs: Long): Either<PersistenceError, RedisLockAcquireResult>

    public suspend fun releaseLock(
        key: K,
        lock: ByteArray,
    ): Either<PersistenceError, RedisLockReleaseResult>
}
