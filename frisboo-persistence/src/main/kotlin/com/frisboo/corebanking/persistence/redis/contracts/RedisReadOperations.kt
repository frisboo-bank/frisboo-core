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
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisExistsResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanPageResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor

public interface RedisReadOperations<K : Any, V : Any> {
    public suspend fun ping(): Either<PersistenceError, Boolean>

    public suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>>

    public suspend fun exists(key: K): Either<PersistenceError, RedisExistsResult>

    public suspend fun scanCount(batchSize: Long? = null): Either<PersistenceError, Long>

    public suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPageResult<K>>
}
