package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.adapters.lettuce.results.fromLettuceResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDecrementResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisIncrementResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisPingResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfAbsentResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfPresentResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.KeyScanCursor
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import java.security.MessageDigest

@OptIn(ExperimentalLettuceCoroutinesApi::class)
internal class RedisLettuceCommandsImpl(
    private val delegate: RedisCoroutinesCommands<ByteArray, ByteArray>,
) : RedisCommands {

    private companion object {
        val ACQUIRE_LOCK_SCRIPT = """
            local key = KEYS[1]
            local value = ARGV[1]
            local ttl = tonumber(ARGV[2])
            local result = redis.call("SET", key, value, "NX", "PX", ttl)
            if result == false then
                return 0 -- Lock already exists
            end
            return 1 -- Lock acquired successfully
        """.trimIndent().toByteArray(Charsets.UTF_8)

        val RELEASE_LOCK_SCRIPT = """
            local currentValue = redis.call("GET", KEYS[1])
            if currentValue == false then
                return 0 -- Key does not exist, treat as mismatch
            end
            if currentValue ~= ARGV[1] then
                return 0 -- Lock value does not match, cannot release
            end
            redis.call("DEL", KEYS[1])
            return 1 -- Lock released successfully
        """.trimIndent().toByteArray(Charsets.UTF_8)

        private val COMPARE_AND_SET_SCRIPT = """
            local key = KEYS[1]
            local expected = ARGV[1]
            local newValue = ARGV[2]
            local ttl = ARGV[3]

            local function do_set()
              if ttl ~= "__NULL__" and tonumber(ttl) and tonumber(ttl) > 0 then
                redis.call("SET", key, newValue, "PX", tonumber(ttl))
              else
                redis.call("SET", key, newValue)
              end
            end

            local current = redis.call("GET", key)

            if current == false then
                if expected == "__NULL__" then
                    do_set()
                    return 1 -- Success: Key did not exist and was set
                else
                    return -1 -- Key not found, but expected was not null
                end
            end

            if current ~= expected then
                return -2 -- Mismatch: Current value does not match expected
            end

            do_set()
            return 1 -- Success: Value matched expected and was updated
        """.trimIndent().toByteArray(Charsets.UTF_8)
    }

    // Simple in-memory cache of script SHA per script content (keyed by script SHA1 hex)
    private val scriptShaCache: MutableMap<String, String> = mutableMapOf()

    private fun sha1Hex(data: ByteArray): String = MessageDigest.getInstance("SHA-1")
        .digest(data)
        .joinToString(separator = "") { "%02x".format(it) }

    private suspend fun <T> executeScriptWithCache(
        script: ByteArray,
        type: ScriptOutputType,
        keys: Array<ByteArray>,
        values: Array<ByteArray>,
    ): T {
        val scriptKey = sha1Hex(script)

        // Try cached sha -> evalsha
        val cachedSha = scriptShaCache[scriptKey]
        if (cachedSha != null) {
            try {
                @Suppress("UNCHECKED_CAST")
                return delegate.evalsha(cachedSha, type, keys = keys, values = values) as T
            } catch (e: Exception) {
                // fallthrough to load/eval on NOSCRIPT or other script-related errors
                if (e.message?.contains("NOSCRIPT", ignoreCase = true) == false) throw e
            }
        }

        // Try loading the script into Redis script cache and run via evalsha
        try {
            val sha = delegate.scriptLoad(script)
            scriptShaCache[scriptKey] = sha
            @Suppress("UNCHECKED_CAST")
            return delegate.evalsha(sha, type, keys = keys, values = values) as T
        } catch (e: Exception) {
            // Fallback to direct EVAL if scriptLoad/evalsha not supported or fails (cluster/no-cache)
            @Suppress("UNCHECKED_CAST")
            return delegate.eval(script, type, keys = keys, values = values) as T
        }
    }

    override suspend fun ping(): RedisPingResult {
        val result = delegate.ping()
        return RedisPingResult.fromLettuceResult(result)
    }

    override suspend fun get(key: ByteArray): RedisGetResult {
        val result = delegate.get(key)
        return RedisGetResult.fromLettuceResult(result)
    }

    override suspend fun set(key: ByteArray, value: ByteArray): RedisSetResult {
        val result = delegate.setGet(key, value)
        return RedisSetResult.fromLettuceResult(result)
    }

    override suspend fun set(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetResult {
        val result = delegate.setGet(key, value, SetArgs.Builder.px(ttlMs))
        return RedisSetResult.fromLettuceResult(result)
    }

    override suspend fun setIfAbsent(key: ByteArray, value: ByteArray): RedisSetIfAbsentResult {
        val result = delegate.set(key, value, SetArgs.Builder.nx())
        return RedisSetIfAbsentResult.fromLettuceResult(result)
    }

    override suspend fun setIfAbsent(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetIfAbsentResult {
        val result = delegate.set(key, value, SetArgs.Builder.nx().px(ttlMs))
        return RedisSetIfAbsentResult.fromLettuceResult(result)
    }

    override suspend fun setIfPresent(key: ByteArray, value: ByteArray): RedisSetIfPresentResult {
        val result = delegate.getset(key, value)
        return RedisSetIfPresentResult.fromLettuceResult(result)
    }

    override suspend fun setIfPresent(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetIfPresentResult {
        val prev = delegate.get(key) ?: return RedisSetIfPresentResult.KeyNotFound
        val result = delegate.set(key, value, SetArgs.Builder.xx().px(ttlMs))
        return RedisSetIfPresentResult.fromLettuceResult(result)
    }

    override suspend fun compareAndSet(
        key: ByteArray,
        expected: ByteArray,
        value: ByteArray,
    ): RedisCompareAndSetResult {
        val result = executeScriptWithCache<Long>(
            script = COMPARE_AND_SET_SCRIPT,
            type = ScriptOutputType.INTEGER,
            keys = arrayOf(key),
            values = arrayOf(expected, value, "__NULL__".toByteArray()),
        )
        return RedisCompareAndSetResult.fromLettuceResult(result)
    }

    override suspend fun compareAndSet(
        key: ByteArray,
        expected: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): RedisCompareAndSetResult {
        val ttlArg = ttlMs.toString().toByteArray()
        val result = executeScriptWithCache<Long>(
            script = COMPARE_AND_SET_SCRIPT,
            type = ScriptOutputType.INTEGER,
            keys = arrayOf(key),
            values = arrayOf(expected, value, ttlArg),
        )
        return RedisCompareAndSetResult.fromLettuceResult(result)
    }

    override suspend fun del(key: ByteArray): Long? = delegate.del(key)

    override suspend fun exists(key: ByteArray): Long? =
        delegate.exists(key)

    override suspend fun pexpire(key: ByteArray, ttlMs: Long): Boolean? =
        delegate.pexpire(key, ttlMs)

    override suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): Pair<RedisScanCursor, List<ByteArray>> {
        val mappedCursor = cursor?.let { ScanCursor.of(it.value) } ?: ScanCursor.INITIAL
        val scanArgs = ScanArgs.Builder.limit(count).match(pattern)
        val result: KeyScanCursor<ByteArray>? = delegate.scan(mappedCursor, scanArgs)
        return RedisScanResult.fromLettuceResult(result)
    }

    override suspend fun acquireLock(key: ByteArray, lock: ByteArray, ttlMs: Long): RedisLockAcquireResult {
        val result = executeScriptWithCache<Long>(
            script = ACQUIRE_LOCK_SCRIPT,
            type = ScriptOutputType.INTEGER,
            keys = arrayOf(key),
            values = arrayOf(lock, ttlMs.toString().toByteArray()),
        )
        return RedisLockAcquireResult.fromLettuceResult(result)
    }

    override suspend fun releaseLock(
        key: ByteArray,
        lock: ByteArray,
    ): RedisLockReleaseResult {
        val result = executeScriptWithCache<Long>(
            script = RELEASE_LOCK_SCRIPT,
            type = ScriptOutputType.INTEGER,
            keys = arrayOf(key),
            values = arrayOf(lock),
        )
        return RedisLockReleaseResult.fromLettuceResult(result)
    }

    override suspend fun increment(key: ByteArray, delta: Long): RedisIncrementResult {
        val result = delegate.incrby(key, delta)
        return RedisIncrementResult.fromLettuceResult(result)
    }

    override suspend fun decrement(key: ByteArray, delta: Long): RedisDecrementResult {
        val result = delegate.decrby(key, delta)
        return RedisDecrementResult.fromLettuceResult(result)
    }
}
