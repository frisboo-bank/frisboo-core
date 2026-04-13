package com.frisboo.corebanking.persistence.redis

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_BATCH_SIZE
import com.frisboo.corebanking.persistence.redis.contracts.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.RedisConnection
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.models.RedisCodec
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.models.RedisScanPage
import java.security.SecureRandom
import kotlin.time.Duration

public class RedisOperationsImpl<K : Any, V : Any>(
    private val connection: RedisConnection,
    private val operationTimeout: Duration,
    private val lockTtl: Duration,
    private val codec: RedisCodec<K, V>,
) : RedisOperations<K, V> {

    override suspend fun ping(): Either<PersistenceError, String> =
        doExecuteOperation("PING") { it.ping() }.map { it.orEmpty() }

    override suspend fun set(
        key: K,
        value: V,
    ): Either<PersistenceError, V?> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()

        @SuppressWarnings("sonar:S4087")
        val raw = doExecuteOperation("SET") { it.set(key = k, value = v) }.bind()
        raw?.let { deserializeValue(it).bind() }
    }

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, V?> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()

        val raw = doExecuteOperation("SET") { it.set(key = k, value = v, ttlMs = ttlMs) }.bind()
        raw?.let { deserializeValue(it).bind() }
    }

    override suspend fun get(key: K): Either<PersistenceError, V?> = either {
        val k = serializeKey(key).bind()

        val rawResult = doExecuteOperation("GET") { it.get(key = k) }.bind()
        rawResult?.let { deserializeValue(it).bind() }
    }

    override suspend fun del(key: K): Either<PersistenceError, Boolean> = either {
        val k = serializeKey(key).bind()
        val result = doExecuteOperation("DEL") { it.del(key = k) }.bind()
        ensureNotNull(result) { PersistenceError.OperationFailed("DEL returned null") }
        result == 1L
    }

    override suspend fun exists(key: K): Either<PersistenceError, Boolean> = either {
        val k = serializeKey(key).bind()
        val result = doExecuteOperation("EXISTS") { it.exists(key = k) }.bind()
        ensureNotNull(result) { PersistenceError.OperationFailed("EXISTS returned null") }
        result
    }

    override suspend fun pexpire(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, Boolean> = either {
        val k = serializeKey(key).bind()
        val result = doExecuteOperation("PEXPIRE") { it.pexpire(key = k, ttlMs = ttlMs) }.bind()
        ensureNotNull(result) { PersistenceError.OperationFailed("PEXPIRE returned null") }
        result
    }

    override suspend fun scanCount(
        batchSize: Long?,
    ): Either<PersistenceError, Long> = either {
        var totalCount = 0L
        val effectiveBatchSize = batchSize ?: REDIS_DEFAULT_SCAN_BATCH_SIZE
        var cursor = RedisScanCursor.INITIAL

        do {
            val result = doExecuteOperation("SCAN_COUNT") {
                it.scan(cursor = cursor, count = effectiveBatchSize, pattern = scanPattern())
            }.bind()
            ensureNotNull(result) { PersistenceError.OperationFailed("SCAN returned null") }
            val (nextCursor, keys) = result
            totalCount += keys.size
            cursor = nextCursor
        } while (!cursor.isFinished)
        totalCount
    }

    override suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPage<K>> = either {
        val scanCursor = cursor ?: RedisScanCursor.INITIAL
        val result = doExecuteOperation("SCAN") {
            it.scan(cursor = scanCursor, count = limit.toLong(), pattern = scanPattern())
        }.bind() ?: (RedisScanCursor.FINISHED to emptyList())
        val (nextCursor, keys) = result

        RedisScanPage(
            keys = keys.map { deserializeKey(it).bind() },
            nextCursor = nextCursor,
        )
    }

    override suspend fun acquireLock(key: K): Either<PersistenceError, ByteArray> = either {
        val lockKey = serializeLockKey(key).bind()
        val lockValue = SecureRandom().generateSeed(16)
        val result = doExecuteOperation("ACQUIRE_LOCK") {
            it.setnx(key = lockKey, value = lockValue, ttlMs = lockTtl.inWholeMilliseconds)
        }.bind()
        when (result) {
            true -> lockValue
            else -> raise(PersistenceError.LockAlreadyHeld("Lock for key $key is already held"))
        }
    }

    override suspend fun releaseLock(key: K, lock: ByteArray): Either<PersistenceError, Unit> = either {
        val lockKey = serializeLockKey(key).bind()
        val result = doExecuteOperation("RELEASE_LOCK") {
            it.evalRecord(
                """
                    -- KEYS[1]: lockKey
                    -- ARGV[1]: token
                    if redis.call("get", KEYS[1]) ~= ARGV[1] then
                        return {0, nil}
                    end
                    return {redis.call("del", KEYS[1]), nil}
                """.trimIndent(),
                keys = arrayOf(lockKey),
                args = arrayOf(lock),
                valueType = ByteArray::class.java,
            )
        }.bind()
        ensureNotNull(result) { PersistenceError.OperationFailed("RELEASE_LOCK returned null") }
        val (status, _) = result
        when (status) {
            1L -> Unit
            else -> raise(PersistenceError.LockNotHeld("Lock for key $key not held or token mismatch"))
        }
    }

    override suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, V?> = doSet(key, value, null, lock)

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, V?> = doSet(key, value, ttlMs, lock)

    private suspend fun doSet(
        key: K,
        value: V,
        ttlMs: Long?,
        lock: ByteArray,
    ): Either<PersistenceError, V?> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()
        val lockKey = serializeLockKey(key).bind()

        val result = doExecuteOperation("EVAL setWithLock") {
            it.evalRecord(
                """
                local key = KEYS[1]
                local lockKey = KEYS[2]
                local value = ARGV[1]
                local lock = ARGV[2]
                local ttl = ARGV[3]
                local old = nil

                if redis.call("get", lockKey) ~= lock then
                    return {-1, nil}
                end
                if redis.call("pttl", lockKey) <= 0 then
                    return {-2, nil}
                end

                if ttl ~= "0" then
                    old = redis.call("set", key, value, "PX", ttl, "GET")
                else
                    old = redis.call("set", key, value, "GET")
                end
                return {0, old}
            """.trimIndent(),
                keys = arrayOf(k, lockKey),
                args = arrayOf(v, lock, (ttlMs?.toString() ?: "0").toByteArray(Charsets.UTF_8)),
                valueType = ByteArray::class.java,
            )
        }.bind()
        ensureNotNull(result) { PersistenceError.OperationFailed("SET with lock returned null") }
        val (status, oldBytes) = result

        when (status) {
            -1L -> raise(PersistenceError.LockNotHeld("Lock for key $key not held (wrong token)"))
            -2L -> raise(PersistenceError.LockNotHeld("Lock for key $key expired"))
            0L -> oldBytes?.let { deserializeValue(it).bind() }
            else -> raise(PersistenceError.OperationFailed("Unexpected status: $status"))
        }
    }

    private suspend inline fun <T> doExecuteOperation(
        operationName: String,
        noinline block: suspend (command: RedisCommands) -> T?,
    ): Either<PersistenceError, T?> = executeWithTimeout(
        timeout = operationTimeout,
        block = { connection.withCommands(block) },
        onTimeout = { duration, _ ->
            PersistenceError.OperationTimeout(
                "$operationName timed out after ${duration.inWholeMilliseconds} ms",
                duration = duration,
            )
        },
        onError = { e -> PersistenceError.OperationFailed("$operationName failed: ${e.message}", e) },
    )

    private fun serializeKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeKey(key)
    private fun deserializeKey(data: ByteArray): Either<PersistenceError, K> = codec.deserializeKey(data)
    private fun serializeValue(value: V): Either<PersistenceError, ByteArray> = codec.serializeValue(value)
    private fun deserializeValue(data: ByteArray): Either<PersistenceError, V> = codec.deserializeValue(data)
    private fun scanPattern(): ByteArray = codec.scanPattern
    private fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeLockKey(key)
}
