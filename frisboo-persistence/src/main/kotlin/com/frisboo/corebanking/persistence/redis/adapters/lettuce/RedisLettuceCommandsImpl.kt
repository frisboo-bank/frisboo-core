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
package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsAcquireLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsReleaseLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsSetWithLockResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

@OptIn(ExperimentalLettuceCoroutinesApi::class)
internal class RedisLettuceCommandsImpl(
    private val delegate: RedisCoroutinesCommands<ByteArray, ByteArray>,
) : RedisCommands {
    private companion object {
        val ACQUIRE_LOCK_SCRIPT = """
            local current = redis.call("GET", KEYS[1])
            if current ~= false then
                return string.char(1)
            end
            redis.call("SET", KEYS[1], ARGV[1], "PX", tonumber(ARGV[2]))
            return string.char(0)
            """.trimIndent().toByteArray(Charsets.UTF_8)

        val RELEASE_LOCK_SCRIPT = """
            local currentValue = redis.call("GET", KEYS[1])
            if currentValue == false then
                return string.char(1)
            end
            if currentValue ~= ARGV[1] then
                return string.char(2)
            end
            redis.call("DEL", KEYS[1])
            return string.char(0)
            """.trimIndent().toByteArray(Charsets.UTF_8)

        val COMPARE_AND_SET_SCRIPT = """
            local key = KEYS[1]
            local expected = ARGV[1]
            local isNullExpected = ARGV[2]
            local newValue = ARGV[3]
            local ttl = tonumber(ARGV[4])

            local function do_set()
                if ttl and ttl > 0 then
                    redis.call("SET", key, newValue, "PX", ttl)
                else
                    redis.call("SET", key, newValue)
                end
            end

            local current = redis.call("GET", key)

            if current == false then
                if isNullExpected ~= "1" then
                    return string.char(1)
                end
                do_set()
                return string.char(0)
            end

            if isNullExpected == "1" then
                return string.char(2)
            end

            if current ~= expected then
                return string.char(2)
            end

            do_set()
            return string.char(0) .. current
            """.trimIndent().toByteArray(Charsets.UTF_8)

        val SET_WITH_LOCK_SCRIPT = """
            local lockKey = KEYS[1]
            local dataKey = KEYS[2]
            local expectedLock = ARGV[1]
            local newValue = ARGV[2]
            local ttl = tonumber(ARGV[3])

            local current = redis.call("GET", lockKey)
            if current == false then
                return string.char(1)
            end
            if current ~= expectedLock then
                return string.char(2)
            end

            local oldValue = redis.call("GET", dataKey)
            if ttl and ttl > 0 then
                redis.call("SET", dataKey, newValue, "PX", ttl)
            else
                redis.call("SET", dataKey, newValue)
            end

            if oldValue == false then
                return string.char(0)
            else
                return string.char(0) .. oldValue
            end
            """.trimIndent().toByteArray(Charsets.UTF_8)
    }

    override suspend fun ping(): Boolean = delegate.ping() == "PONG"

    override suspend fun get(key: ByteArray): ByteArray? {
        require(key.isNotEmpty()) { "Key must not be empty" }
        return delegate.get(key)
    }

    override suspend fun setGet(
        key: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): ByteArray? {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(value.isNotEmpty()) { "Value must not be empty" }
        require(ttlMs >= 0) { "TTL must be non-negative" }

        return when (ttlMs) {
            0L -> delegate.setGet(key, value)
            else -> delegate.setGet(key, value, SetArgs.Builder.px(ttlMs))
        }
    }

    override suspend fun setIfAbsent(
        key: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): Boolean {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(value.isNotEmpty()) { "Value must not be empty" }
        require(ttlMs >= 0) { "TTL must be non-negative" }

        val args = SetArgs.Builder.nx()
        if (ttlMs > 0L) {
            args.px(ttlMs)
        }
        return delegate.set(key, value, args) == "OK"
    }

    override suspend fun compareAndSet(
        key: ByteArray,
        expected: ByteArray?,
        value: ByteArray,
        ttlMs: Long,
    ): RedisCommandsCompareAndSetResult {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(value.isNotEmpty()) { "Value must not be empty" }
        require(ttlMs >= 0) { "TTL must be non-negative" }

        val ttlArg = ttlMs.toString().toByteArray(Charsets.UTF_8)
        val isNullExpected = (if (expected == null) "1" else "0").toByteArray(Charsets.UTF_8)

        val raw = delegate.eval<ByteArray>(
            script = COMPARE_AND_SET_SCRIPT,
            type = ScriptOutputType.VALUE,
            keys = arrayOf(key),
            values = arrayOf(expected ?: ByteArray(0), isNullExpected, value, ttlArg),
        )

        return when (val status = raw?.get(0)?.toInt()) {
            0 -> {
                val previousValue = if (raw.size > 1) raw.copyOfRange(1, raw.size) else null
                RedisCommandsCompareAndSetResult.Success(previousValue)
            }

            1 -> RedisCommandsCompareAndSetResult.KeyNotFound
            2 -> RedisCommandsCompareAndSetResult.Mismatch
            else -> throw IllegalStateException("Unknown status $status")
        }
    }

    override suspend fun del(key: ByteArray): ByteArray? {
        require(key.isNotEmpty()) { "Key must not be empty" }

        return delegate.getdel(key)
    }

    override suspend fun exists(key: ByteArray): Boolean {
        require(key.isNotEmpty()) { "Key must not be empty" }

        return delegate.exists(key)?.let { it == 1L } == true
    }

    override suspend fun pexpire(
        key: ByteArray,
        ttlMs: Long,
    ): Boolean {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(ttlMs > 0) { "TTL must be greater than 0" }

        return delegate.pexpire(key, ttlMs) == true
    }

    override suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): Pair<RedisScanCursor, List<ByteArray?>?>? {
        require(count > 0) { "Count must be greater than 0" }
        require(pattern.isNotEmpty()) { "Pattern must not be empty" }

        val mappedCursor = cursor?.toRedisLettuceScanCursor() ?: ScanCursor.INITIAL
        val scanArgs = ScanArgs.Builder.limit(count).match(pattern)
        val result = delegate.scan(mappedCursor, scanArgs) ?: return null

        return RedisScanCursor.of(result.cursor) to result.keys
    }

    override suspend fun acquireLock(key: ByteArray, lock: ByteArray, ttlMs: Long): RedisCommandsAcquireLockResult {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(lock.isNotEmpty()) { "Lock value must not be empty" }
        require(ttlMs > 0) { "TTL must be greater than 0" }

        val raw = delegate.eval<ByteArray>(
            script = ACQUIRE_LOCK_SCRIPT,
            type = ScriptOutputType.VALUE,
            keys = arrayOf(key),
            values = arrayOf(lock, ttlMs.toString().toByteArray(Charsets.UTF_8)),
        )
        return when (val status = raw?.get(0)) {
            0.toByte() -> RedisCommandsAcquireLockResult.Acquired(lock)
            1.toByte() -> RedisCommandsAcquireLockResult.AlreadyHeld
            else -> throw IllegalStateException("Unknown status $status")
        }
    }

    override suspend fun releaseLock(
        key: ByteArray,
        lock: ByteArray,
    ): RedisCommandsReleaseLockResult {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(lock.isNotEmpty()) { "Lock value must not be empty" }

        val raw = delegate.eval<ByteArray>(
            script = RELEASE_LOCK_SCRIPT,
            type = ScriptOutputType.VALUE,
            keys = arrayOf(key),
            values = arrayOf(lock),
        )

        return when (val status = raw?.get(0)?.toInt()) {
            0 -> RedisCommandsReleaseLockResult.Released
            1 -> RedisCommandsReleaseLockResult.NotHeld
            2 -> RedisCommandsReleaseLockResult.Mismatch
            else -> throw IllegalStateException("Unknown status $status")
        }
    }

    override suspend fun setWithLock(
        lockKey: ByteArray,
        dataKey: ByteArray,
        lock: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): RedisCommandsSetWithLockResult {
        require(lockKey.isNotEmpty()) { "Lock key must not be empty" }
        require(dataKey.isNotEmpty()) { "Data key must not be empty" }
        require(lock.isNotEmpty()) { "Lock value must not be empty" }
        require(ttlMs >= 0) { "TTL must be non-negative" }

        val ttlArg = ttlMs.toString().toByteArray(Charsets.UTF_8)

        val raw = delegate.eval<ByteArray>(
            script = SET_WITH_LOCK_SCRIPT,
            type = ScriptOutputType.VALUE,
            keys = arrayOf(lockKey, dataKey),
            values = arrayOf(lock, value, ttlArg),
        )

        return when (val status = raw?.get(0)?.toInt()) {
            0 -> {
                val previousValue = if (raw.size > 1) raw.copyOfRange(1, raw.size) else null
                RedisCommandsSetWithLockResult.Success(previousValue)
            }

            1 -> RedisCommandsSetWithLockResult.LockMissing
            2 -> RedisCommandsSetWithLockResult.LockMismatch
            else -> throw IllegalStateException("Unknown status $status")
        }
    }

    override suspend fun increment(
        key: ByteArray,
        delta: Long,
    ): Long? {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(delta >= 0) { "Delta must be non-negative" }

        return delegate.incrby(key, delta)
    }

    override suspend fun decrement(
        key: ByteArray,
        delta: Long,
    ): Long? {
        require(key.isNotEmpty()) { "Key must not be empty" }
        require(delta >= 0) { "Delta must be non-negative" }

        return delegate.decrby(key, delta)
    }
}
