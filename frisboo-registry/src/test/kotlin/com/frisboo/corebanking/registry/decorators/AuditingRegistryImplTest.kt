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

import com.frisboo.corebanking.registry.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.registry.contracts.RegistryAuditEmitter
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryAuditAction
import com.frisboo.corebanking.registry.models.RegistryAuditEvent
import com.frisboo.corebanking.registry.models.RegistryScope
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

/** Stable HMAC key for deterministic test assertions. */
private val TEST_HMAC_KEY = "test-hmac-key-for-registry-audit".toByteArray(Charsets.UTF_8)

internal class AuditingRegistryImplTest :
    StringSpec({
        val scope = RegistryScope(name = "audit-test", team = "test-team")

        "put emits audit event with action registry.put and correct scope" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                registry.put("customer-1", "value-1")

                captured.size shouldBe 1
                val event = captured.single()
                event.action shouldBe RegistryAuditAction.PUT
                event.scope shouldBe scope.prefix
            }
        }

        "put records PutResult.Created in details when key is new" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                val result = registry.put("new-key", "value")

                result shouldBe PutResult.Created
                captured.single().details["result"] shouldBe "Created"
            }
        }

        "put records PutResult.Updated in details when key exists" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                registry.put("existing-key", "value-1")
                val result = registry.put("existing-key", "value-2")

                result shouldBe PutResult.Updated
                captured.size shouldBe 2
                captured.last().details["result"] shouldBe "Updated"
            }
        }

        "evict emits audit event with action registry.evict" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                registry.put("evict-key", "value")
                registry.evict("evict-key")

                captured.size shouldBe 2
                val event = captured.last()
                event.action shouldBe RegistryAuditAction.EVICT
                event.scope shouldBe scope.prefix
            }
        }

        "setTTL emits audit event with action registry.setTTL and ttl_ms in details" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)
                val ttl = 30.seconds

                registry.put("ttl-key", "value")
                registry.setTTL("ttl-key", ttl)

                captured.size shouldBe 2
                val event = captured.last()
                event.action shouldBe RegistryAuditAction.SET_TTL
                event.details["ttl_ms"] shouldBe ttl.inWholeMilliseconds.toString()
            }
        }

        "resourceId is SHA-256 hashed and never contains raw key" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)
                val key = "raw-sensitive-key"

                registry.put(key, "value")

                val resourceId = captured.single().resourceId
                resourceId shouldStartWith "hmac-sha256:"
                (resourceId?.contains(key) ?: false) shouldBe false
            }
        }

        "same key always produces same sanitized resourceId" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)
                val key = "stable-key"

                registry.put(key, "value-1")
                registry.put(key, "value-2")

                captured.size shouldBe 2
                captured[0].resourceId shouldBe captured[1].resourceId
            }
        }

        "audit emission failure is swallowed and delegate result is returned" {
            runBlocking {
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val failingEmitter = RegistryAuditEmitter { throw IllegalStateException("boom") }
                val registry = AuditingRegistryImpl(delegate, scope, failingEmitter, TEST_HMAC_KEY)

                val result = registry.put("safe-key", "safe-value")
                val stored = registry.get("safe-key")

                result shouldBe PutResult.Created
                stored shouldBe "safe-value"
            }
        }

        "get contains size keys are delegated without audit" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                delegate.put("existing", "value")

                registry.get("existing") shouldBe "value"
                registry.contains("existing") shouldBe true
                registry.size() shouldBe 1L
                registry.keys() shouldBe setOf("existing")

                captured.size shouldBe 0
            }
        }

        "getOrPut emits audit when key is new" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                val result = registry.getOrPut("missing") { "created-by-getOrPut" }

                result shouldBe GetOrPutResult.Created("created-by-getOrPut")
                captured.size shouldBe 1
                val event = captured.single()
                event.action shouldBe RegistryAuditAction.GET_OR_PUT
                event.details["result"] shouldBe "Created"
            }
        }

        "getOrPut does not emit audit when key already exists" {
            runBlocking {
                val captured = mutableListOf<RegistryAuditEvent>()
                val emitter = CapturingRegistryAuditEmitter(captured)
                val delegate = InMemoryRegistryImpl<String, String>(scope)
                val registry = AuditingRegistryImpl(delegate, scope, emitter, TEST_HMAC_KEY)

                delegate.put("existing", "value")

                val result = registry.getOrPut("existing") { "should-not-be-used" }

                result shouldBe GetOrPutResult.Found("value")
                captured.size shouldBe 0
            }
        }
    })

private class CapturingRegistryAuditEmitter(
    private val events: MutableList<RegistryAuditEvent>,
) : RegistryAuditEmitter {
    override suspend fun emit(event: RegistryAuditEvent) {
        events.add(event)
    }
}
