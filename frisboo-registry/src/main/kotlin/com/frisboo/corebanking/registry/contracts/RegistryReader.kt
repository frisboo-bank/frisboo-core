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
package com.frisboo.corebanking.registry.contracts

/**
 * Read-only operations on a scoped registry.
 *
 * Implementations must be safe for concurrent use from coroutines.
 */
public interface RegistryReader<K : Any, V : Any> {
    /**
     * Returns the value associated with [key], or `null` if absent or expired.
     */
    public suspend fun get(key: K): V?

    /**
     * Returns `true` if the registry contains a non-expired entry for [key].
     */
    public suspend fun contains(key: K): Boolean

    /**
     * Returns the number of non-expired entries; O(N) SCAN on distributed implementations.
     */
    public suspend fun size(): Long
}
