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
package com.frisboo.corebanking.persistence.redis.models

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError

public class RedisCodec<K, V>(
    prefix: String,
    lockPrefix: String,
    private val keySerializer: PersistenceSerializer<K>,
    private val valueSerializer: PersistenceSerializer<V>,
) {
    private companion object {
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

    public fun serializeKey(key: K): Either<PersistenceError, ByteArray> = serializeWithCatch("Key") {
        val kb = keySerializer.serialize(key)
        if (kb.isEmpty()) {
            throw IllegalArgumentException("Serialized key must not be empty")
        }

        val combined = prefixBytes + kb
        if (combined.size > MAX_KEY_SIZE) {
            throw IllegalArgumentException("Serialized key too large: ${combined.size} bytes")
        }
        combined
    }

    public fun deserializeKey(prefixedKey: ByteArray): Either<PersistenceError, K> = either {
        ensure(prefixedKey.size >= prefixBytes.size) {
            return Either.Left(PersistenceError.DeserializationFailed("Key shorter than prefix"))
        }
        ensure(prefixBytes.indices.all { prefixedKey[it] == prefixBytes[it] }) {
            return Either.Left(PersistenceError.DeserializationFailed("Key prefix mismatch"))
        }

        val keyBytes = prefixedKey.copyOfRange(prefixBytes.size, prefixedKey.size)
        return deserializeWithCatch("Key") { keySerializer.deserialize(keyBytes) }
    }

    public fun serializeValue(value: V): Either<PersistenceError, ByteArray> =
        serializeWithCatch("Value") { valueSerializer.serialize(value) }

    public fun deserializeValue(data: ByteArray): Either<PersistenceError, V> =
        deserializeWithCatch("Value") { valueSerializer.deserialize(data) }

    public fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> = serializeWithCatch("Lock key") {
        val kb = keySerializer.serialize(key)
        if (kb.isEmpty()) throw IllegalArgumentException("Serialized lock key must not be empty")
        val combined = lockFullPrefixBytes + kb
        if (combined.size > MAX_KEY_SIZE) {
            throw IllegalArgumentException("Serialized lock key too large: ${combined.size} bytes")
        }
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
}
