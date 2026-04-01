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
package com.frisboo.corebanking.data.idempotency.contracts

/**
 * Read-only idempotency contract.
 */
public interface IdempotencyChecker {
    /**
     * Checks whether the given key has already been processed.
     *
     * @param key idempotency key.
     * @return `true` when key is already processed.
     */
    public suspend fun isProcessed(key: String): Boolean

    /**
     * Returns a stored result for a previously processed key.
     *
     * @param key idempotency key.
     * @return serialized result payload or `null` when unavailable.
     */
    public suspend fun getResult(key: String): ByteArray?
}
