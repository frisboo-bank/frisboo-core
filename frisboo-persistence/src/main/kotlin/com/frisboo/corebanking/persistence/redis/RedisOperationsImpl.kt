package com.frisboo.corebanking.persistence.redis

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.result
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_LOCK_SIZE
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_MAX_SCAN_ITERATIONS
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_BATCH_SIZE
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisConnection
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDeleteResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.models.RedisCodec
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.models.RedisScanPage
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Duration

internal class RedisOperationsImpl<K : Any, V : Any>(
    private val connection: RedisConnection,
    private val operationTimeout: Duration,
    private val lockTtl: Duration,
    private val codec: RedisCodec<K, V>,
) : RedisOperations<K, V> {

    init {
        require(operationTimeout.isPositive()) { "operationTimeout must be positive" }
        require(lockTtl.isPositive()) { "lockTtl must be positive" }
    }

    /**
     * Pings the Redis server to check if the connection is alive.
     *
     * @return True if the server responds with "PONG", false otherwise
     */
    override suspend fun ping(): Either<PersistenceError, Boolean> =
        doExecuteOperation("PING") { it.ping() }

    /**
     * Sets a value for the given key with an optional TTL. If the key already exists, it will be overwritten.
     *
     * @param key The key to set
     * @param value The value to associate with the key
     * @return The previous value associated with the key, or null if there was no previous value
     */
    override suspend fun set(
        key: K,
        value: V,
    ): Either<PersistenceError, RedisSetResult<ByteArray?>> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()

        val previous = doExecuteOperation("SET") { it.setGet(key = k, value = v) }.bind()
        RedisSetResult.Success(previous)
    }

    /**
     * Sets a value for the given key with a specified TTL.
     *
     * @param key The key to set
     * @param value The value to associate with the key
     * @param ttlMs Time-to-live in milliseconds for the key-value pair. Must be positive.
     * @return The previous value associated with the key, or null if there was no previous value
     */
    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisSetResult<ByteArray?>> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }

        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()

        val previous = doExecuteOperation("SET") { it.setGet(key = k, value = v, ttlMs = ttlMs) }.bind()
        RedisSetResult.Success(previous)
    }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key The key to retrieve
     * @return The value associated with the key, or null if the key does not exist
     */
    override suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>> = either {
        val key = serializeKey(key).bind()

        when (val rawResult = doExecuteOperation("GET") { it.get(key = key) }.bind()) {
            is ByteArray -> RedisGetResult.Found(deserializeValue(rawResult).bind())
            null -> RedisGetResult.NotFound
        }
    }

    /**
     * Deletes the given key from Redis.
     *
     * @param key The key to delete
     * @return A result indicating whether the key was deleted or not found
     */
    override suspend fun del(key: K): Either<PersistenceError, RedisDeleteResult> = either {
        val k = serializeKey(key).bind()

        when (val result = doExecuteOperation("DEL") { it.del(key = k) }.bind()) {
            true -> RedisDeleteResult.Deleted
            false -> RedisDeleteResult.KeyNotFound
        }
    }

    /**
     * Checks if the given key exists in Redis.
     *
     * @param key The key to check
     * @return True if the key exists, false otherwise
     */
    override suspend fun exists(key: K): Either<PersistenceError, Boolean> = either {
        val k = serializeKey(key).bind()

        when(val result = doExecuteOperation("EXISTS") { it.exists(key = k) }.bind()) {
            true -> RedisExistsResult.Exists
            false -> RedisExistsResult.NotFound
            null -> raise(PersistenceError.OperationFailed("EXISTS command returned null"))
        }
    }

    /**
     * Sets a time-to-live (TTL) for the given key.
     *
     * @param key The key to set the TTL for
     * @param ttlMs Time-to-live in milliseconds. Must be positive.
     * @return True if the TTL was set successfully, false if the key does not exist or the TTL could not be set
     */
    override suspend fun pexpire(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, Boolean> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }

        val k = serializeKey(key).bind()

        val result = doExecuteOperation("PEXPIRE") { it.pexpire(key = k, ttlMs = ttlMs) }.bind()
        result == true
    }

    /**
     * Counts the total number of keys matching the scan pattern by iterating through all keys using SCAN.
     *
     * @param batchSize Optional batch size for each SCAN iteration.
     * @return The total count of keys matching the scan pattern,
     *         or an error if the operation fails or exceeds max iterations
     */
    override suspend fun scanCount(
        batchSize: Long?,
    ): Either<PersistenceError, Long> = either {
        var totalCount = 0L
        val effectiveBatchSize = batchSize?.coerceAtLeast(1L) ?: REDIS_DEFAULT_SCAN_BATCH_SIZE
        ensure(effectiveBatchSize > 0) { PersistenceError.OperationFailed("batchSize must be positive") }

        var cursor = RedisScanCursor.INITIAL
        var iterations = 0L

        do {
            val result = doExecuteOperation("SCAN_COUNT") {
                it.scan(cursor = cursor, count = effectiveBatchSize, pattern = scanPattern())
            }.bind()

            val (nextCursor, keys) = result
            totalCount += keys.size
            iterations++

            ensure(iterations <= REDIS_DEFAULT_MAX_SCAN_ITERATIONS) {
                PersistenceError.OperationFailed("SCAN exceeded max iterations ($REDIS_DEFAULT_MAX_SCAN_ITERATIONS)")
            }
            cursor = nextCursor
        } while (!cursor.isFinished)
        totalCount
    }

    /**
     * Retrieves a page of keys matching the scan pattern, starting from the given cursor.
     *
     * @param cursor The cursor to start scanning from. If null, starts from the beginning.
     * @param limit The maximum number of keys to return in this page. Must be positive.
     * @return A page of keys and the next cursor, or an error if the operation fails
     */
    override suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPage<K>> = either {
        ensure(limit > 0) { PersistenceError.OperationFailed("limit must be positive") }

        val scanCursor = cursor ?: RedisScanCursor.INITIAL

        val result = doExecuteOperation("SCAN") {
            it.scan(cursor = scanCursor, count = limit.toLong(), pattern = scanPattern())
        }.bind()

        val (nextCursor, keys) = result

        RedisScanPage(
            keys = keys.map { deserializeKey(it).bind() },
            nextCursor = nextCursor,
        )
    }

    /**
     * Acquires a lock for the given key.
     *
     * @param key The key to lock
     * @return The lock token if the lock was acquired successfully, or an error if the lock is already held
     */
    override suspend fun acquireLock(key: K): Either<PersistenceError, com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult> =
        either {
            val lockKey = serializeLockKey(key).bind()
            val lockValue = ByteArray(REDIS_DEFAULT_LOCK_SIZE).also { ThreadLocalRandom.current().nextBytes(it) }

            val result = doExecuteOperation("ACQUIRE_LOCK") {
                it.acquireLock(key = lockKey, lock = lockValue, ttlMs = lockTtl.inWholeMilliseconds)
            }.bind()

            when (result) {
                is com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult.Acquired -> result
                else -> raise(PersistenceError.LockAlreadyHeld("Lock for key $key is already held"))
            }
        }

    /**
     * Releases a lock for the given key.
     *
     * @param key The key to unlock
     * @param lock The lock token that was returned when the lock was acquired
     * @return Unit if the lock was released successfully,
     *         or an error if the lock is not held or the token does not match
     */
    override suspend fun releaseLock(
        key: K,
        lock: ByteArray,
    ): Either<PersistenceError, com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult> =
        either {
            val lockKey = serializeLockKey(key).bind()
            val result = doExecuteOperation("RELEASE_LOCK") {
                it.releaseLock(key = lockKey, lock = lock)
            }.bind()

            when (result) {
                is com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult.Released -> com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult.Released
                else -> raise(PersistenceError.LockNotHeld("Lock for key $key not held or token mismatch"))
            }
        }

    /**
     * Sets a value for the given key only if the provided lock token is valid.
     *
     * @param key The key to set
     * @param value The value to associate with the key
     * @param lock The lock token that must be valid to perform the set operation
     * @return The previous value associated with the key, or null if there was no previous value,
     *         or an error if the lock is not held or has expired
     */
    override suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult> = doSet(key, value, null, lock)

    /**
     * Sets a value for the given key with a specified TTL, only if the provided lock token is valid.
     *
     * @param key The key to set
     * @param value The value to associate with the key
     * @param ttlMs Time-to-live in milliseconds for the key-value pair. Must be positive.
     * @param lock The lock token that must be valid to perform the set operation
     * @return The previous value associated with the key, or null if there was no previous value,
     *         or an error if the lock is not held, has expired, or if ttlMs is not positive
     */
    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult> = doSet(key, value, ttlMs, lock)

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
    ): Either<PersistenceError, RedisCompareAndSetResult> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()
        val expectedBytes = expected?.let { serializeValue(it).bind() }

        val code = doExecuteOperation("EVAL compareAndSet") {
            it.compareAndSet(key = k, expected = expectedBytes, value = v)
        }.bind()
        code
    }

    override suspend fun increment(key: K, delta: Long): Either<PersistenceError, Long> = either {
        val k = serializeKey(key).bind()

        val result = doExecuteOperation("INCREMENTBY") { it.increment(key = k, delta = delta) }.bind()
        result ?: raise(PersistenceError.OperationFailed("INCRBY returned null"))
    }

    override suspend fun decrement(key: K, delta: Long): Either<PersistenceError, Long> = either {
        val k = serializeKey(key).bind()

        val result = doExecuteOperation("DECREMENTBY") { it.decrement(key = k, delta = delta) }.bind()
        result ?: raise(PersistenceError.OperationFailed("DECRBY returned null"))
    }

    // internal methods

    private suspend fun doSet(
        key: K,
        value: V,
        ttlMs: Long?,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()
        val lockKey = serializeLockKey(key).bind()

        val lockStatus = doExecuteOperation("CHECK_LOCK") {
            it.compareAndSet(key = lockKey, expected = lock, value = lock)
        }.bind()

        check(lockStatus is RedisCompareAndSetResult.Success) { "Lock for key $key not held or has expired" }

        // Perform the set while applying TTL via the setReturningPrevious API.
        // This separates TTL responsibility from the compareAndSet/lock-check.
        val previousRaw = if (ttlMs != null) {
            doExecuteOperation("SET with lock") { it.setGet(key = k, value = v, ttlMs = ttlMs) }.bind()
        } else {
            doExecuteOperation("SET with lock") { it.setGet(key = k, value = v) }.bind()
        }

        // If previousRaw is null, there was no previous value. Otherwise deserialize and return it.
        val previousValue = previousRaw?.let { deserializeValue(it).bind() }
        RedisSetResult.Success(previousRaw)

    }

    private suspend fun <T> doExecuteOperation(
        operationName: String,
        block: suspend (command: RedisCommands) -> T,
    ): Either<PersistenceError, T> = executeWithTimeout(
        timeout = operationTimeout,
        block = { connection.withCommands(block) },
        onTimeout = { duration, _ ->
            PersistenceError.OperationTimeout(
                "$operationName timed out after ${duration.inWholeMilliseconds} ms",
                duration = duration,
            )
        },
        onError = { e ->
            PersistenceError.OperationFailed("$operationName failed: ${e.message}", e)
        },
    )

    private fun serializeKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeKey(key)
    private fun deserializeKey(data: ByteArray): Either<PersistenceError, K> = codec.deserializeKey(data)
    private fun serializeValue(value: V): Either<PersistenceError, ByteArray> = codec.serializeValue(value)
    private fun deserializeValue(data: ByteArray): Either<PersistenceError, V> = codec.deserializeValue(data)
    private fun scanPattern(): ByteArray = codec.scanPattern
    private fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeLockKey(key)
}
