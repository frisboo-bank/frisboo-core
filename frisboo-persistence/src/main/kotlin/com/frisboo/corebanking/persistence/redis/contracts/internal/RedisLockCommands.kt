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
package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsAcquireLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsReleaseLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsSetWithLockResult

internal interface RedisLockCommands {
    suspend fun acquireLock(key: ByteArray, lock: ByteArray, ttlMs: Long): RedisCommandsAcquireLockResult

    suspend fun releaseLock(key: ByteArray, lock: ByteArray): RedisCommandsReleaseLockResult

    suspend fun setWithLock(
        lockKey: ByteArray,
        dataKey: ByteArray,
        lock: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): RedisCommandsSetWithLockResult
}
