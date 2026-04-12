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
package com.frisboo.corebanking.registry.decorators

import arrow.core.Either
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.contracts.RegistryAuditEmitter
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryPutResult
import com.frisboo.corebanking.registry.models.RegistryAuditAction
import com.frisboo.corebanking.registry.models.RegistryAuditEvent
import com.frisboo.corebanking.registry.models.RegistryContainsResult
import com.frisboo.corebanking.registry.models.RegistryGetResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.RegistrySetTtlResult
import com.frisboo.corebanking.registry.models.RegistrySizeResult
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

public class AuditingRegistryImpl<K : Any, V : Any>(
    private val delegate: Registry<K, V>,
    private val scope: RegistryScope,
    private val auditEmitter: RegistryAuditEmitter,
    auditHmacKey: ByteArray,
) : Registry<K, V> {

    private val hmacKey = SecretKeySpec(auditHmacKey.copyOf(), "HmacSHA256")
    override suspend fun get(key: K): Either<RegistryError, RegistryGetResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun contains(key: K): Either<RegistryError, RegistryContainsResult> {
        TODO("Not yet implemented")
    }

    override suspend fun size(): Either<RegistryError, RegistrySizeResult> {
        TODO("Not yet implemented")
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): Either<RegistryError, RegistryPutResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>> {
        TODO("Not yet implemented")
    }

    override suspend fun evict(key: K): Either<RegistryError, RegistryEvictResult> {
        TODO("Not yet implemented")
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<RegistryError, RegistryPage<K>> {
        TODO("Not yet implemented")
    }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<RegistryError, RegistrySetTtlResult> {
        TODO("Not yet implemented")
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

//    override suspend fun put(
//        key: K,
//        value: V,
//        ttl: Duration?,
//    ): RegistryPutResult {
//        val result = delegate.put(key, value, ttl)
//        emitAudit(
//            RegistryAuditEvent(
//                action = RegistryAuditAction.PUT,
//                scope = scope.prefix,
//                resourceId = sanitizeKey(key),
//                success = result is RegistryPutResult.Created || result is RegistryPutResult.Updated,
//                details =
//                    buildMap {
//                        put("result", result.simpleName())
//                        if (ttl != null) put("ttl_ms", ttl.inWholeMilliseconds.toString())
//                    },
//            ),
//        )
//        return result
//    }
//
//    override suspend fun getOrPut(
//        key: K,
//        ttl: Duration?,
//        factory: suspend () -> V,
//    ): RegistryGetOrPutResult<V> {
//        val result = delegate.getOrPut(key, ttl, factory)
//        if (result is RegistryGetOrPutResult.Created) {
//            emitAudit(
//                RegistryAuditEvent(
//                    action = RegistryAuditAction.GET_OR_PUT,
//                    scope = scope.prefix,
//                    resourceId = sanitizeKey(key),
//                    success = true,
//                    details =
//                        buildMap {
//                            put("result", "Created")
//                            if (ttl != null) put("ttl_ms", ttl.inWholeMilliseconds.toString())
//                        },
//                ),
//            )
//        }
//        return result
//    }
//
//    override suspend fun evict(key: K): RegistryEvictResult {
//        val result = delegate.evict(key)
//        emitAudit(
//            RegistryAuditEvent(
//                action = RegistryAuditAction.EVICT,
//                scope = scope.prefix,
//                resourceId = sanitizeKey(key),
//                success = result is RegistryEvictResult.Evicted,
//                details = mapOf("result" to result.simpleName()),
//            ),
//        )
//        return result
//    }
//
//    override suspend fun setTTL(
//        key: K,
//        ttl: Duration,
//    ): RegistrySetTtlResult {
//        val result = delegate.setTTL(key, ttl)
//        emitAudit(
//            RegistryAuditEvent(
//                action = RegistryAuditAction.SET_TTL,
//                scope = scope.prefix,
//                resourceId = sanitizeKey(key),
//                success = result is RegistrySetTtlResult.Applied,
//                details =
//                    mapOf(
//                        "ttl_ms" to ttl.inWholeMilliseconds.toString(),
//                        "result" to result.simpleName(),
//                    ),
//            ),
//        )
//        return result
//    }
//
//    private suspend fun emitAudit(event: RegistryAuditEvent) {
//        runCatching { auditEmitter.emit(event) }
//            .onFailure { error ->
//                if (error is CancellationException) throw error
//                logger.error(error) { "Audit emission failed for ${event.action.value}" }
//            }
//    }
//
//    private fun Any.simpleName(): String = this::class.simpleName ?: "unknown"
//
//    @OptIn(ExperimentalStdlibApi::class)
//    private fun sanitizeKey(key: K): String {
//        val raw = key.toString().toByteArray(Charsets.UTF_8)
//        val mac = Mac.getInstance("HmacSHA256")
//        mac.init(hmacKey)
//        return "hmac-sha256:${mac.doFinal(raw).toHexString()}"
//    }
}
