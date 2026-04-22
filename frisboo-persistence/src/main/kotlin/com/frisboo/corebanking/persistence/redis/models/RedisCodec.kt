package com.frisboo.corebanking.persistence.redis.models

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError

/**
 * Codec for serializing/deserializing keys and values with a prefix for Redis storage.
 *
 * @param K Key type
 * @param V Value type
 * @property prefix Prefix for all keys (must be non-blank and max 1KB)
 * @property lockPrefix Prefix for lock keys (must be non-blank and max 1KB)
 * @property keySerializer Serializer for keys
 * @property valueSerializer Serializer for values
 */
public class RedisCodec<K, V>(
    private val prefix: String,
    lockPrefix: String,
    private val keySerializer: PersistenceSerializer<K>,
    private val valueSerializer: PersistenceSerializer<V>,
) {

    private companion object {
        // Upper bound for combined key size (prefix + serialized key) to avoid unexpected huge keys.
        private const val MAX_KEY_SIZE: Int = 10 * 1024 // 10 KiB
    }

    private val prefixBytes: ByteArray = "$prefix:".toByteArray(Charsets.UTF_8)
    private val lockFullPrefixBytes: ByteArray = "$prefix:$lockPrefix:".toByteArray(Charsets.UTF_8)

    init {
        require(prefix.isNotBlank()) { "Prefix must be non-blank" }
        require(lockPrefix.isNotBlank()) { "Lock prefix must be non-blank" }
        require(prefixBytes.size <= 1024) { "Prefix too long (max 1KB)" }
        require(lockFullPrefixBytes.size <= 1024) { "Lock prefix too long (max 1KB)" }
    }

    public val scanPattern: ByteArray = "$prefix:*".toByteArray(Charsets.UTF_8)

    public fun serializeKey(key: K): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Key") {
            val kb = keySerializer.serialize(key)
            if (kb.isEmpty()) throw IllegalArgumentException("Serialized key must not be empty")
            val combined = prefixBytes + kb
            if (combined.size > MAX_KEY_SIZE) throw IllegalArgumentException("Serialized key too large: ${combined.size} bytes")
            combined
        }

    public fun deserializeKey(prefixedKey: ByteArray): Either<PersistenceError, K> {
        when {
            prefixedKey.size < prefixBytes.size ->
                return Either.Left(PersistenceError.DeserializationFailed("Key shorter than prefix"))
            !prefixedKey.startsWith(prefixBytes) ->
                return Either.Left(PersistenceError.DeserializationFailed("Key prefix mismatch"))
        }
        val keyBytes = prefixedKey.copyOfRange(prefixBytes.size, prefixedKey.size)
        return deserializeWithCatch("Key") { keySerializer.deserialize(keyBytes) }
    }

    public fun serializeValue(value: V): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Value") { valueSerializer.serialize(value) }

    public fun deserializeValue(data: ByteArray): Either<PersistenceError, V> =
        deserializeWithCatch("Value") { valueSerializer.deserialize(data) }

    public fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Lock key") {
            val kb = keySerializer.serialize(key)
            if (kb.isEmpty()) throw IllegalArgumentException("Serialized lock key must not be empty")
            val combined = lockFullPrefixBytes + kb
            if (combined.size > MAX_KEY_SIZE) throw IllegalArgumentException("Serialized lock key too large: ${combined.size} bytes")
            combined
        }

    private inline fun serializeWithCatch(
        field: String,
        block: () -> ByteArray,
    ): Either<PersistenceError.SerializationFailed, ByteArray> =
        Either.catch(block).mapLeft { PersistenceError.SerializationFailed("$field serialization failed", it) }

    private inline fun <T> deserializeWithCatch(
        field: String,
        block: () -> T,
    ): Either<PersistenceError, T> =
        Either.catch(block).mapLeft { PersistenceError.DeserializationFailed("$field deserialization failed", it) }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        this.size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
}
