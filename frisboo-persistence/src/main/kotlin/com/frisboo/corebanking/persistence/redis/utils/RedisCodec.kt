package com.frisboo.corebanking.persistence.redis.utils

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError

public class RedisCodec<K, V>(
    private val prefix: String,
    private val keySerializer: PersistenceSerializer<K>,
    private val valueSerializer: PersistenceSerializer<V>,
) {

    private val prefixBytes = "$prefix:".toByteArray()

    public fun serializeKey(key: K): Either<PersistenceError, ByteArray> =
        Either.catch { prefixBytes + keySerializer.serialize(key) }
            .mapLeft { PersistenceError.SerializationFailed("key serialization failed", it) }

    public fun deserializeKey(prefixedKey: ByteArray): Either<PersistenceError, K> =
        Either.catch { keySerializer.deserialize(stripPrefix(prefixedKey)) }
            .mapLeft { PersistenceError.DeserializationFailed("key deserialization failed", it) }

    public fun serializeValue(value: V): Either<PersistenceError, ByteArray> =
        Either.catch { valueSerializer.serialize(value) }
            .mapLeft { PersistenceError.SerializationFailed("value serialization failed", it) }

    public fun deserializeValue(data: ByteArray): Either<PersistenceError, V> =
        Either.catch { valueSerializer.deserialize(data) }
            .mapLeft { PersistenceError.DeserializationFailed("value deserialization failed", it) }

    public val scanPattern: ByteArray get() = "$prefix:*".toByteArray()

    /**
     * Strips the prefix from the given key.
     */
    private fun stripPrefix(prefixedKey: ByteArray): ByteArray =
        prefixedKey.copyOfRange(prefixBytes.size, prefixedKey.size)
}
