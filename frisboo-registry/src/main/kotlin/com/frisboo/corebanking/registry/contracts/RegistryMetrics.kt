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
 * Read-only counters for a [Registry] instance.
 *
 * Implementations are lock-free ([java.util.concurrent.atomic.AtomicLong]) and safe
 * for concurrent reads from coroutines. Bind to Micrometer gauges or scrape periodically
 * without affecting registry throughput.
 */
public interface RegistryMetrics {
    /** Total cache hits (successful [Registry.get] or [Registry.contains] lookups). */
    public val hitCount: Long

    /** Total cache misses ([Registry.get] returning `null` or [Registry.contains] returning `false`). */
    public val missCount: Long

    /** Total entries written ([Registry.put] created/updated or [Registry.getOrPut] factory invoked). */
    public val putCount: Long

    /** Total entries removed via [Registry.evict]. */
    public val evictionCount: Long

    /** Total operations that returned a `Failed` result. */
    public val failureCount: Long

    public companion object
}
