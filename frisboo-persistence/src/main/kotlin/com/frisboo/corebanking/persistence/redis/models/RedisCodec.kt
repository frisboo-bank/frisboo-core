package com.frisboo.corebanking.persistence.redis.models

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError

public class RedisCodec<K, V>(
    private val prefix: String,
    lockPrefix: String,
    private val keySerializer: PersistenceSerializer<K>,
    private val valueSerializer: PersistenceSerializer<V>,
) {

    private val prefixBytes: ByteArray = "$prefix:".toByteArray(Charsets.UTF_8)
    private val lockFullPrefixBytes: ByteArray = "$prefix:$lockPrefix:".toByteArray(Charsets.UTF_8)

    init {
        require(prefix.isNotBlank()) { "Prefix must be non-blank" }
        require(lockPrefix.isNotBlank()) { "Lock prefix must be non-blank" }
        require(prefixBytes.size <= 1024) { "Prefix too long (max 1KB)" }
        require(lockFullPrefixBytes.size <= 1024) { "Lock prefix too long (max 1KB)" }
    }

    public val scanPattern: ByteArray get() = "$prefix:*".toByteArray(Charsets.UTF_8)

    public fun serializeKey(key: K): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Key") { prefixBytes + keySerializer.serialize(key) }

    public fun deserializeKey(prefixedKey: ByteArray): Either<PersistenceError, K> {
        if (prefixedKey.size < prefixBytes.size) {
            return Either.Left(PersistenceError.DeserializationFailed("Key shorter than prefix"))
        }
        for (i in prefixBytes.indices) {
            if (prefixedKey[i] != prefixBytes[i]) {
                return Either.Left(PersistenceError.DeserializationFailed("Key prefix mismatch"))
            }
        }
        val keyBytes = prefixedKey.copyOfRange(prefixBytes.size, prefixedKey.size)
        return deserializeWithCatch("Key") { keySerializer.deserialize(keyBytes) }
    }

    public fun serializeValue(value: V): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Value") { valueSerializer.serialize(value) }

    public fun deserializeValue(data: ByteArray): Either<PersistenceError, V> =
        deserializeWithCatch("Value") { valueSerializer.deserialize(data) }

    public fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Lock key") { lockFullPrefixBytes + keySerializer.serialize(key) }

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
}
