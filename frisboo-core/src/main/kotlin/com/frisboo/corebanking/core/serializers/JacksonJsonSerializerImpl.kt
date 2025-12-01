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
package com.frisboo.corebanking.core.serializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.frisboo.corebanking.core.contracts.Serializer
import com.frisboo.corebanking.core.domain.errors.SerializationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
public class JacksonJsonSerializerImpl(
    private val objectMapper: ObjectMapper,
) : Serializer {
    private companion object {
        private val logger = KotlinLogging.logger { }
    }

    @Throws(SerializationException::class)
    override fun <T> deserialize(
        data: ByteArray,
        clazz: Class<T>,
    ): T =
        try {
            objectMapper.readValue(data, clazz)
        } catch (err: Exception) {
            logger.error(err) { "Deserialization error for class: ${clazz.name}" }
            throw SerializationException(err, clazz)
        }

    @Throws(SerializationException::class)
    override fun serializeToBytes(data: Any): ByteArray =
        try {
            objectMapper.writeValueAsBytes(data)
        } catch (err: Exception) {
            logger.error(err) { "Serialization to bytes error for class: ${data::class.java.name}" }
            throw SerializationException(err, data::class.java)
        }

    @Throws(SerializationException::class)
    override fun serializeToString(data: Any): String =
        try {
            objectMapper.writeValueAsString(data)
        } catch (err: Exception) {
            logger.error(err) { "Serialization to string error for class: ${data::class.java.name}" }
            throw SerializationException(err, data::class.java)
        }
}
