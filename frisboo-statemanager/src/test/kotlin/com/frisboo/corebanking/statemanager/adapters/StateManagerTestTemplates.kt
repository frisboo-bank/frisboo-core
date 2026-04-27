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
package com.frisboo.corebanking.statemanager.adapters

import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.REGISTRY_DEFAULT_MAX_PAGE_SIZE
import com.frisboo.corebanking.statemanager.models.StateManagerContainsResult
import com.frisboo.corebanking.statemanager.models.StateManagerEvictResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetResult
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult
import com.frisboo.corebanking.statemanager.models.StateManagerSizeResult
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.coreshouldBeRight
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
internal object StateManagerTestTemplates {

    fun putAndGetRoundTrip(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.string(),
            Arb.string(),
            Arb.boolean(),
        ) { key, value, saved ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            when (saved) {
                true -> {
                    stateManager.put(key, value) shouldBeRight  StateManagerPutResult.Created(value))
                    stateManager.get(key) shouldBeRight  StateManagerGetResult.Found(value))
                }

                false -> stateManager.get(key) shouldBeRight  StateManagerGetResult.NotFound)
            }
        }
    }

    fun putAndGetRoundTripOfBinaryData(
        stateManagerProvider: suspend () -> StateManager<String, ByteArray>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.string(),
            Arb.byteArray(Arb.int(0..4096), Arb.byte()),
        ) { key, value ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            stateManager.put(key, value) shouldBeRight  StateManagerPutResult.Created(value))

            stateManager.get(key) shouldBeRight  )
                .shouldBeInstanceOf<StateManagerGetResult.Found<ByteArray>>().value.contentEquals(value) shouldBe true
        }
    }

    fun putAndGetRoundTripHandleConcurrentPutsAndGets(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.string(),
            Arb.int(2..20),
            Arb.boolean(),
        ) { key, concurrentWriters, alsoEvict ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            val values = (1..concurrentWriters).map { "value-$it" }

            coroutineScope {
                val writers = values.map { value ->
                    async(Dispatchers.IO) {
                        stateManager.put(key, value)
                    }
                }
                val evictor = if (alsoEvict) {
                    async(Dispatchers.IO) {
                        delay(5)
                        stateManager.evict(key)
                    }
                } else null

                (writers + listOfNotNull(evictor)).awaitAll()
            }

            when (val result = stateManager.get(key) shouldBeRight  )) {
                is StateManagerGetResult.Found -> result.value shouldBeIn values
                StateManagerGetResult.NotFound -> alsoEvict shouldBe true
            }
        }
    }

    fun concurrentEvictAndPutDoNotLeaveTheKeyInAnInconsistentState(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(1..20), Arb.string(1..20)) { key, value ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            coroutineScope {
                val putJob = async(Dispatchers.IO) {
                    delay(5)
                    stateManager.put(key, value)
                }
                val evictJob = async(Dispatchers.IO) {
                    stateManager.evict(key)
                }

                putJob.await() shouldBeRight  )
                evictJob.await() shouldBeRight  )
            }

            when (val getResult = stateManager.get(key) shouldBeRight  )) {
                is StateManagerGetResult.Found -> getResult.value shouldBe value
                is StateManagerGetResult.NotFound -> { /* evict won */
                }
            }
        }
    }

    fun putWithExistingKeyReturnsUpdated(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(), Arb.string(), Arb.string()) { key, initialValue, newValue ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            stateManager.put(key, initialValue) shouldBeRight  StateManagerPutResult.Created(initialValue))
            stateManager.put(key, newValue)
                 shouldBeRight  StateManagerPutResult.Updated(previousValue = initialValue, newValue = newValue))
            stateManager.get(key) shouldBeRight  StateManagerGetResult.Found(newValue))
        }
    }

    fun putHandleConcurrentPutsToTheSameKey(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.string(),
            Arb.int(2..20),
        ) { key, concurrentWriters ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            val values = (1..concurrentWriters).map { "value-$it" }

            coroutineScope {
                val jobs = List(concurrentWriters) {
                    async(Dispatchers.IO) {
                        delay(5)
                        stateManager.put(key, values[it]) shouldBeRight  )
                    }
                }
                jobs.awaitAll()
            }

            stateManager.get(key) shouldBeRight  )
                .shouldBeInstanceOf<StateManagerGetResult.Found<String>>().value shouldBeIn values
        }
    }

    fun putWithNegativeTTLReturnsInvalidArgument(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        val stateManager = stateManagerProvider()
        val result = stateManager.put("key", "value", ttl = (-1).milliseconds)

        result.shouldBeLeft().shouldBeInstanceOf<StateManagerError.InvalidArgument>().also {
            it.name shouldBe "ttl"
            it.message shouldContain "TTL must be >= 1ms"
        }
    }

    fun putWithTTLExpiresTheRecord(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(),
            Arb.string(),
            Arb.int(10..20),
        ) { key, value, ttl ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            val ttlMs = ttl.milliseconds

            stateManager.put(key, value, ttl = ttlMs) shouldBeRight  StateManagerPutResult.Created(value))
            stateManager.get(key) shouldBeRight  StateManagerGetResult.Found(value))

            delay(ttlMs + 10.milliseconds)

            stateManager.get(key) shouldBeRight  StateManagerGetResult.NotFound)
        }
    }

    fun containsChecksIfKeyExistsWithoutRetrievingValue(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(), Arb.string(), Arb.boolean()) { key, value, saved ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            when (saved) {
                true -> {
                    stateManager.put(key, value) shouldBeRight  StateManagerPutResult.Created(value))
                    stateManager.contains(key) shouldBeRight  StateManagerContainsResult.Found)
                }

                false -> stateManager.contains(key) shouldBeRight  StateManagerContainsResult.NotFound)
            }
        }
    }

    fun returnTheNumberOfRecordsSavedInTheCurrentScope(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.list(Arb.string())) { keys ->
            val stateManager = stateManagerProvider()
            val uniqueKeys = keys.toSet()

            // Clean up before test
            uniqueKeys.forEach { stateManager.evict(it) shouldBeRight  ) }

            uniqueKeys.forEachIndexed { index, key ->
                stateManager.put(key, "value$index") shouldBeRight  )
            }

            stateManager.size() shouldBeRight  StateManagerSizeResult.Size(uniqueKeys.size.toLong()))
        }
    }

    fun concurrentSizeAndModificationsYieldEventuallyCorrectCount(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.int(20..100),
            Arb.int(2..20),
        ) { totalKeys, concurrentWriters ->
            val stateManager = stateManagerProvider()
            val keyPrefix = "size-test-${UUID.randomUUID()}"
            val allKeys = (1..totalKeys).map { "$keyPrefix-$it" }

            allKeys.forEach { stateManager.evict(it) shouldBeRight  ) }

            val sizes = coroutineScope {
                val writerJob = List(concurrentWriters) { writerIndex ->
                    async(Dispatchers.IO) {
                        allKeys.filterIndexed { index, _ -> index % concurrentWriters == writerIndex }
                            .forEach { key ->
                                stateManager.put(key, "v")
                                delay(2)
                            }
                    }
                }
                val sizes = mutableListOf<Long>()
                val readerJob = async(Dispatchers.IO) {
                    repeat(30) {
                        stateManager.size().onRight { sizes.add(it.count) }
                        delay(2)
                    }
                }
                (writerJob + readerJob).awaitAll()
                sizes
            }

            stateManager.size() shouldBeRight  StateManagerSizeResult.Size(totalKeys.toLong()))
            sizes.forEach {
                it shouldBeGreaterThanOrEqual 0L
                it shouldBeLessThanOrEqual totalKeys.toLong()
            }
        }
    }

    fun getOrPutRetrievesOrSavesValue(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(), Arb.string(), Arb.boolean()) { key, value, exists ->
            val statemanager = stateManagerProvider()
            statemanager.evict(key) shouldBeRight  )

            if (exists) {
                statemanager.put(key, value) shouldBeRight  StateManagerPutResult.Created(value))
            }

            var factoryCalled = false
            val result = statemanager.getOrPut(key) {
                factoryCalled = true
                value
            }

            when (exists) {
                true -> {
                    result shouldBeRight  StateManagerGetOrPutResult.Found(value))
                    factoryCalled shouldBe false
                }

                false -> {
                    result shouldBeRight  StateManagerGetOrPutResult.Created(value))
                    factoryCalled shouldBe true
                    statemanager.get(key) shouldBeRight  StateManagerGetResult.Found(value))
                }
            }
        }
    }

    fun getOrPutHandleConcurrentCallsToSameKey(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(), Arb.string(), Arb.int(2..100)) { key, value, concurrentCalls ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            val factoryCalls = AtomicInteger(0)

            coroutineScope {
                val jobs = List(concurrentCalls) {
                    async(Dispatchers.IO) {
                        stateManager.getOrPut(key) {
                            delay(1)
                            factoryCalls.incrementAndGet()
                            value
                        }
                    }
                }
                jobs.awaitAll().forEach { it shouldBeRight  ) }
            }

            factoryCalls.get() shouldBe 1
            stateManager.get(key) shouldBeRight  StateManagerGetResult.Found(value))
        }
    }

    fun getOrPutPropagatesFactoryExceptions(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        val stateManager = stateManagerProvider()
        val result = stateManager.getOrPut("fail-key") { throw RuntimeException("boom") }

        result.shouldBeLeft().shouldBeInstanceOf<StateManagerError.ComputationFailed>().also {
            it.message shouldContain "fail-key"
            it.cause.shouldBeInstanceOf<RuntimeException>().message shouldBe "boom"
        }
    }

    fun getOrPutWithNegativeTTLReturnsInvalidArgument(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        val stateManager = stateManagerProvider()
        val result = stateManager.getOrPut("key", ttl = (-1).milliseconds) { "value" }

        result.shouldBeLeft().shouldBeInstanceOf<StateManagerError.InvalidArgument>().also {
            it.name shouldBe "ttl"
            it.message shouldContain "TTL must be >= 1ms"
        }
    }

    fun getOrPutWithTTLExpiresTheRecord(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(),
            Arb.string(),
            Arb.int(10..20),
        ) { key, value, ttl ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            val ttlMs = ttl.milliseconds

            stateManager.getOrPut(key, ttl = ttlMs) { value }
                 shouldBeRight  StateManagerGetOrPutResult.Created(value))
            stateManager.get(key) shouldBeRight  StateManagerGetResult.Found(value))

            delay(ttlMs + 10.milliseconds)

            stateManager.get(key) shouldBeRight  StateManagerGetResult.NotFound)
        }
    }

    fun evictRemovesExistingKey(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(Arb.string(), Arb.string(), Arb.boolean()) { key, value, exists ->
            val stateManager = stateManagerProvider()
            if (exists) {
                stateManager.put(key, value) shouldBeRight  )
            }

            when (exists) {
                true -> stateManager.evict(key) shouldBeRight  StateManagerEvictResult.Evicted(1))
                false -> stateManager.evict(key) shouldBeRight  StateManagerEvictResult.NotFound)
            }

            stateManager.contains(key) shouldBeRight  StateManagerContainsResult.NotFound)
        }
    }

    fun keysPageReturnsPaginatedKeys(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.list(Arb.string(), 1..50),
            Arb.int(10..60),
        ) { keys, limit ->
            val stateManager = stateManagerProvider()
            val uniqueKeys = keys.toSet()

            uniqueKeys.forEach { stateManager.evict(it) shouldBeRight  ) }
            uniqueKeys.forEach { stateManager.put(it, "val-$it") shouldBeRight  ) }

            val retrievedKeys = mutableSetOf<String>()
            var cursor: String? = null
            do {
                val page = stateManager.keysPage(cursor, limit) shouldBeRight  )
                retrievedKeys.addAll(page.items)
                cursor = page.nextCursor
            } while (cursor != null)

            retrievedKeys shouldBe uniqueKeys
        }
    }

    fun keysPageWithInvalidLimitReturnsInvalidArgument(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            Arb.int(min = Int.MIN_VALUE, max = 0),
            Arb.int(min = REGISTRY_DEFAULT_MAX_PAGE_SIZE + 1, max = Int.MAX_VALUE),
        ) { belowMin, aboveMax ->
            val stateManager = stateManagerProvider()
            listOf(belowMin, aboveMax).forEach { invalidLimit ->
                val result = stateManager.keysPage(null, invalidLimit)

                result.shouldBeLeft().shouldBeInstanceOf<StateManagerError.InvalidArgument>().also {
                    it.name shouldBe "limit"
                    it.message shouldBe
                            "Page size must be between 1 and $REGISTRY_DEFAULT_MAX_PAGE_SIZE, got: $invalidLimit"
                }
            }
        }
    }

    fun setTTLChangesExpiryOfExistingRecord(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(),
            Arb.string(),
            Arb.boolean(),
            Arb.int(10..20),
        ) { key, value, exists, ttlMs ->
            val stateManager = stateManagerProvider()
            stateManager.evict(key) shouldBeRight  )

            if (exists) {
                stateManager.put(key, value) shouldBeRight  StateManagerPutResult.Created(value))
            }

            val ttl = ttlMs.milliseconds
            val setResult = stateManager.setTTL(key, ttl)

            when (exists) {
                false -> setResult shouldBeRight  StateManagerSetTtlResult.NotFound)

                true -> {
                    setResult shouldBeRight  StateManagerSetTtlResult.Applied)
                    stateManager.contains(key) shouldBeRight  StateManagerContainsResult.Found)

                    delay(ttl + 10.milliseconds)

                    stateManager.contains(key) shouldBeRight  StateManagerContainsResult.NotFound)
                }
            }
        }
    }

    fun setTTLWithNegativeTTLReturnsInvalidArgument(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        val stateManager = stateManagerProvider()
        val result = stateManager.setTTL("key", (-1).milliseconds)

        result.shouldBeLeft().shouldBeInstanceOf<StateManagerError.InvalidArgument>().also {
            it.name shouldBe "ttl"
            it.message shouldContain "TTL must be >= 1ms"
        }
    }

    fun stressTestManyConcurrentOperationsOnDifferentKeys(
        stateManagerProvider: suspend () -> StateManager<String, String>,
    ): suspend () -> Unit = {
        checkAll(
            PropTestConfig(iterations = 30),
            Arb.int(1..100),
            Arb.int(20..100),
            Arb.int(2..20),
        ) { operationsPerKey, keyCount, concurrentClients ->
            val stateManager = stateManagerProvider()
            val keyPrefix = "stress-test-${UUID.randomUUID()}"
            val keys = (1..keyCount).map { "$keyPrefix-$it" }
            val expectedFinalValues = ConcurrentHashMap<String, String>()

            keys.forEach { stateManager.evict(it) shouldBeRight  ) }

            coroutineScope {
                val jobs = List(concurrentClients) { clientIndex ->
                    async(Dispatchers.IO) {
                        keys.filterIndexed { index, _ -> index % concurrentClients == clientIndex }
                            .forEach { key ->
                                repeat(operationsPerKey) { opIndex ->
                                    when (opIndex % 5) {
                                        0 -> {
                                            val value = "value-$clientIndex-$opIndex"
                                            stateManager.put(key, value) shouldBeRight  )
                                            expectedFinalValues[key] = value
                                        }

                                        1 -> stateManager.get(key) shouldBeRight  )
                                        2 -> stateManager.contains(key) shouldBeRight  )
                                        3 -> stateManager.setTTL(key, 5.seconds) shouldBeRight  )
                                        4 -> {
                                            stateManager.evict(key) shouldBeRight  )
                                            expectedFinalValues.remove(key)
                                        }
                                    }
                                    delay(1)
                                }
                            }
                    }
                }
                jobs.awaitAll()
            }

            keys.forEach { key ->
                val expected = expectedFinalValues[key]
                val actual = stateManager.get(key).getOrNull()

                when (expected) {
                    null -> actual shouldBe StateManagerGetResult.NotFound
                    else -> (actual as? StateManagerGetResult.Found)?.value shouldBe expected
                }
            }
        }
    }
}
