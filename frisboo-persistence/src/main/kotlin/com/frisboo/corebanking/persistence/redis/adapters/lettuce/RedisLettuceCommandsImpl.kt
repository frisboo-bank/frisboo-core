package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.contracts.RedisCommands
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.KeyScanCursor
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public class RedisLettuceCommandsImpl(
    private val delegate: RedisCoroutinesCommands<ByteArray, ByteArray>,
) : RedisCommands {

    override suspend fun ping(): String = delegate.ping()

    override suspend fun get(key: ByteArray): ByteArray? = delegate.get(key)

    override suspend fun set(key: ByteArray, value: ByteArray): ByteArray? = delegate.setGet(key, value)

    override suspend fun set(key: ByteArray, value: ByteArray, ttlMs: Long): ByteArray? =
        delegate.setGet(key, value, SetArgs.Builder.px(ttlMs))

    override suspend fun del(key: ByteArray): Long? = delegate.del(key)

    override suspend fun exists(key: ByteArray): Boolean = delegate.exists(key) == 1L

    override suspend fun exists(vararg keys: ByteArray): Long? = delegate.exists(*keys)

    override suspend fun pexpire(key: ByteArray, ttlMs: Long): Boolean? = delegate.pexpire(key, ttlMs)

    override suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): Pair<RedisScanCursor, List<ByteArray>> {
        val mappedCursor = cursor?.let { ScanCursor.of(it.value) } ?: ScanCursor.INITIAL
        val scanArgs = ScanArgs.Builder.limit(count).match(pattern)
        val result: KeyScanCursor<ByteArray>? = delegate.scan(mappedCursor, scanArgs)
        val nextCursor = RedisScanCursor.of(result?.cursor)
        return nextCursor to (result?.keys ?: emptyList())
    }

    override suspend fun <T : Any> evalRecord(
        script: String,
        keys: Array<ByteArray>,
        args: Array<ByteArray>,
        valueType: Class<T>,
    ): Pair<Long, T?> {
        val raw = delegate.eval<Any>(script = script, type = ScriptOutputType.MULTI, keys = keys, values = args)
        require(raw is List<*>) { "Expected MULTI response (List), got ${raw?.javaClass}" }

        val status = (raw.getOrNull(0) as? Number)?.toLong() ?: -999L
        val rawValue = raw.getOrNull(1)
        val value = when {
            rawValue == null -> null
            valueType.isInstance(rawValue) -> valueType.cast(rawValue)
            else -> throw IllegalArgumentException(
                "Expected ${valueType.simpleName}, got ${rawValue::class.simpleName}",
            )
        }
        return status to value
    }

    override suspend fun setnx(key: ByteArray, value: ByteArray, ttlMs: Long): Boolean =
        delegate.set(key, value, SetArgs.Builder.nx().px(ttlMs)) == "OK"
}
