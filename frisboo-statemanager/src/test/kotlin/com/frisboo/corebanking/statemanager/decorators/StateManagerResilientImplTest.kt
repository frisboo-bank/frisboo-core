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
package com.frisboo.corebanking.statemanager.decorators

import arrow.core.Either
import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.contracts.StateManagerResilienceExecutor
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetResult
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

internal class StateManagerResilientImplTest : StringSpec() {
    private class FailingStateManager : StateManager<String, String> {
        override suspend fun get(key: String) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun contains(key: String) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun size() = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun put(
            key: String,
            value: String,
        ) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun put(
            key: String,
            value: String,
            ttl: kotlin.time.Duration,
        ) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun getOrPut(
            key: String,
            factory: suspend () -> String,
        ) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun getOrPut(
            key: String,
            ttl: kotlin.time.Duration,
            factory: suspend () -> String,
        ) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun evict(key: String) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun keysPage(
            cursor: String?,
            limit: Int,
        ) = Either.Left(StateManagerError.Communication("fail"))

        override suspend fun setTTL(
            key: String,
            ttl: kotlin.time.Duration,
        ) = Either.Left(StateManagerError.Communication("fail"))
    }

    private class PassingStateManager : StateManager<String, String> {
        private val store = mutableMapOf<String, String>()

        override suspend fun get(key: String) = Either.Right(StateManagerGetResult.Found(store[key]))

        override suspend fun contains(key: String) =
            Either.Right(com.frisboo.corebanking.statemanager.models.StateManagerContainsResult.Found)

        override suspend fun size() =
            Either.Right(
                com.frisboo.corebanking.statemanager.models.StateManagerSizeResult
                    .Size(store.size.toLong()),
            )

        override suspend fun put(
            key: String,
            value: String,
        ) = Either.Right(StateManagerPutResult.Created(value))

        override suspend fun put(
            key: String,
            value: String,
            ttl: kotlin.time.Duration,
        ) = Either.Right(StateManagerPutResult.Created(value))

        override suspend fun getOrPut(
            key: String,
            factory: suspend () -> String,
        ) = Either.Right(StateManagerGetOrPutResult.Created(factory()))

        override suspend fun getOrPut(
            key: String,
            ttl: kotlin.time.Duration,
            factory: suspend () -> String,
        ) = Either.Right(StateManagerGetOrPutResult.Created(factory()))

        override suspend fun evict(key: String) =
            Either.Right(com.frisboo.corebanking.statemanager.models.StateManagerEvictResult.Evicted)

        override suspend fun keysPage(
            cursor: String?,
            limit: Int,
        ) = Either.Right(
            com.frisboo.corebanking.statemanager.models
                .StateManagerPage(listOf(), null),
        )

        override suspend fun setTTL(
            key: String,
            ttl: kotlin.time.Duration,
        ) = Either.Right(com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult.Set)
    }

    private class DirectExecutor : StateManagerResilienceExecutor {
        override suspend fun <T> execute(
            block: suspend () -> T,
            fallback: suspend () -> T,
        ): T =
            try {
                block()
            } catch (e: Exception) {
                fallback()
            }
    }

    init {
        "resilient delegates to fallback when primary fails" {
            val primary = FailingStateManager()
            val fallback = PassingStateManager()
            val resilient = StateManagerResilientImpl(primary, fallback, DirectExecutor())

            runBlocking {
                val get = resilient.get("k")
                get shouldBe Either.Right(StateManagerGetResult.Found(null))

                val put = resilient.put("k", "v")
                put shouldBe Either.Right(StateManagerPutResult.Created("v"))
            }
        }
    }
}
