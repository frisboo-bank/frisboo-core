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
package com.frisboo.corebanking.core.contracts

import com.frisboo.corebanking.core.domain.errors.SerializationException

/**
 * Interface for a generic serializer that provides methods to serialize and deserialize data.
 * This interface is designed to be implemented by classes that handle serialization and deserialization
 * of objects to and from various formats (e.g., JSON, XML, binary).
 */
public interface Serializer {
    /**
     * Deserializes a byte array into an object of the specified class type.
     *
     * @param T The type of the object to deserialize into.
     * @param data The byte array containing the serialized data.
     * @param clazz The `Class` object representing the type to deserialize into.
     * @return The deserialized object of type `T`.
     * @throws SerializationException If deserialization fails due to invalid data or type mismatch.
     */
    @Throws(SerializationException::class)
    public fun <T> deserialize(
        data: ByteArray,
        clazz: Class<T>,
    ): T

    /**
     * Serializes an object into a byte array.
     *
     * @param data The object to serialize.
     * @return A byte array representing the serialized object.
     * @throws SerializationException If serialization fails due to unsupported object types.
     */
    @Throws(SerializationException::class)
    public fun serializeToBytes(data: Any): ByteArray

    /**
     * Serializes an object into a string.
     *
     * @param data The object to serialize.
     * @return A string representation of the serialized object.
     * @throws SerializationException If serialization fails due to unsupported object types.
     */
    @Throws(SerializationException::class)
    public fun serializeToString(data: Any): String
}
