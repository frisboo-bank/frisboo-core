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
package com.frisboo.corebanking.statemanager.adapters.local.caffeine

import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import com.frisboo.corebanking.statemanager.adapters.StateManagerTestTemplates
import com.frisboo.corebanking.statemanager.contracts.StateManager
import io.kotest.core.spec.style.StringSpec
import java.util.UUID

internal class StateManagerCaffeineImplTest : StringSpec() {
    private suspend fun createStateManager(scope: StateManagerScope? = null): StateManager<String, String> {
        val testScope: StateManagerScope =
            scope ?: StateManagerScope(name = "cache", team = "test-${UUID.randomUUID()}")

        return StateManagerCaffeineImpl(
            scope = testScope,
            maximumSize = 10_000,
        )
    }

    private suspend fun createBinaryStateManager(scope: StateManagerScope? = null): StateManager<String, ByteArray> {
        val testScope: StateManagerScope =
            scope ?: StateManagerScope(name = "cache", team = "test-binary-${UUID.randomUUID()}")

        return StateManagerCaffeineImpl(
            scope = testScope,
            maximumSize = 10_000,
        )
    }

    init {

        // ========== Common statemanager tests ==========

        "put and get round-trip" {
            StateManagerTestTemplates.putAndGetRoundTrip { createStateManager() }.invoke()
        }

        "put and get round-trip of binary data" {
            StateManagerTestTemplates.putAndGetRoundTripOfBinaryData { createBinaryStateManager() }.invoke()
        }

        "put and get round-trip handle concurrent puts and gets" {
            StateManagerTestTemplates.putAndGetRoundTripHandleConcurrentPutsAndGets { createStateManager() }.invoke()
        }

        "concurrent evict and put do not leave the key in an inconsistent state" {
            StateManagerTestTemplates
                .concurrentEvictAndPutDoNotLeaveTheKeyInAnInconsistentState { createStateManager() }
                .invoke()
        }

        "put with existing key returns Updated" {
            StateManagerTestTemplates.putWithExistingKeyReturnsUpdated { createStateManager() }.invoke()
        }

        "put handle concurrent puts to the same key" {
            StateManagerTestTemplates.putHandleConcurrentPutsToTheSameKey { createStateManager() }.invoke()
        }

        "put with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.putWithNegativeTTLReturnsInvalidArgument { createStateManager() }.invoke()
        }

        "put with TTL expires the record" {
            StateManagerTestTemplates.putWithTTLExpiresTheRecord { createStateManager() }.invoke()
        }

        "contains checks if key exists without retrieving value" {
            StateManagerTestTemplates.containsChecksIfKeyExistsWithoutRetrievingValue { createStateManager() }.invoke()
        }

        "return the number of records saved in the current scope" {
            StateManagerTestTemplates.returnTheNumberOfRecordsSavedInTheCurrentScope { createStateManager() }.invoke()
        }

        "concurrent size and modifications yield eventually correct count" {
            StateManagerTestTemplates
                .concurrentSizeAndModificationsYieldEventuallyCorrectCount { createStateManager() }
                .invoke()
        }

        "getOrPut retrieves or saves value" {
            StateManagerTestTemplates.getOrPutRetrievesOrSavesValue { createStateManager() }.invoke()
        }

        "getOrPut handle concurrent calls to same key" {
            StateManagerTestTemplates.getOrPutHandleConcurrentCallsToSameKey { createStateManager() }.invoke()
        }

        "getOrPut propagates factory exceptions" {
            StateManagerTestTemplates.getOrPutPropagatesFactoryExceptions { createStateManager() }.invoke()
        }

        "getOrPut with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.getOrPutWithNegativeTTLReturnsInvalidArgument { createStateManager() }.invoke()
        }

        "getOrPut with TTL expires the record" {
            StateManagerTestTemplates.getOrPutWithTTLExpiresTheRecord { createStateManager() }.invoke()
        }

        "evict removes existing key" {
            StateManagerTestTemplates.evictRemovesExistingKey { createStateManager() }.invoke()
        }

        "keysPage returns paginated keys" {
            StateManagerTestTemplates.keysPageReturnsPaginatedKeys { createStateManager() }.invoke()
        }

        "keysPage with invalid limit returns InvalidArgument" {
            StateManagerTestTemplates.keysPageWithInvalidLimitReturnsInvalidArgument { createStateManager() }.invoke()
        }

        "setTTL changes expiry of existing record" {
            StateManagerTestTemplates.setTTLChangesExpiryOfExistingRecord { createStateManager() }.invoke()
        }

        "setTTL with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.setTTLWithNegativeTTLReturnsInvalidArgument { createStateManager() }.invoke()
        }

        "stress test: many concurrent operations on different keys" {
            StateManagerTestTemplates
                .stressTestManyConcurrentOperationsOnDifferentKeys { createStateManager() }
                .invoke()
        }
    }
}
