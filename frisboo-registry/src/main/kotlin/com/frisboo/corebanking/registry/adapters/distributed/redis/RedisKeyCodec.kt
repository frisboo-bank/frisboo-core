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
package com.frisboo.corebanking.registry.adapters.distributed.redis

import com.frisboo.corebanking.registry.contracts.RegistrySerializer
import com.frisboo.corebanking.registry.models.RegistryScope
import io.lettuce.core.ScanArgs

private val SEPARATOR: ByteArray = ":".toByteArray(Charsets.UTF_8)
private val WILDCARD: ByteArray = "*".toByteArray(Charsets.UTF_8)

internal class RedisKeyCodec<K : Any>(
    scope: RegistryScope,
    private val keySerializer: RegistrySerializer<K>,
) {
    private val prefixBytes: ByteArray = scope.prefix.toByteArray(Charsets.UTF_8)

    companion object {
        fun luaArgs(
            serialized: ByteArray,
            ttlMs: Long?,
        ): Array<ByteArray> =
            if (ttlMs != null) {
                arrayOf(serialized, ttlMs.toString().toByteArray(Charsets.UTF_8))
            } else {
                arrayOf(serialized)
            }
    }

    fun encode(key: K): ByteArray = prefixBytes + SEPARATOR + keySerializer.serialize(key)

    fun stripPrefix(redisKey: ByteArray): ByteArray {
        val expectedPrefixSize = prefixBytes.size + SEPARATOR.size
        require(redisKey.size >= expectedPrefixSize) {
            "Redis key too short for scope prefix '${prefixBytes.toString(Charsets.UTF_8)}'"
        }
        val actualPrefix = redisKey.copyOfRange(0, prefixBytes.size)
        require(actualPrefix.contentEquals(prefixBytes)) {
            "Redis key prefix mismatch: expected '${prefixBytes.toString(Charsets.UTF_8)}'"
        }
        return redisKey.copyOfRange(expectedPrefixSize, redisKey.size)
    }

    fun scanPattern(): ByteArray = prefixBytes + SEPARATOR + WILDCARD

    fun scopedScanArgs(): ScanArgs = ScanArgs.Builder.matches(scanPattern())
}
