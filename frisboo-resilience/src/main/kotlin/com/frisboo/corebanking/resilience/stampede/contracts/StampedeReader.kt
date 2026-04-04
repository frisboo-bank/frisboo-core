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
package com.frisboo.corebanking.resilience.stampede.contracts

/**
 * Read contract for cache stampede protection state.
 */
public interface StampedeReader {
    /**
     * Returns cached value for [key] if present and valid.
     */
    public suspend fun <T> getCached(key: String): T?

    /**
     * Indicates whether a computation is currently in progress for [key].
     */
    public suspend fun isComputationInProgress(key: String): Boolean
}
