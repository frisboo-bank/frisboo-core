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
package com.frisboo.corebanking.statemanager.contracts

/**
 * Read-only counters for a [StateManager] instance.
 *
 * Implementations are lock-free ([java.util.concurrent.atomic.AtomicLong]) and safe
 * for concurrent reads from coroutines. Bind to Micrometer gauges or scrape periodically
 * without affecting statemanager throughput.
 */
public interface StateManagerMetrics {

    /**
     * Total cache hits
     */
    public val hitCount: Long

    /**
     * Total cache misses.
     */
    public val missCount: Long

    /**
     * Total entries written
     */
    public val putCount: Long

    /**
     * Total entries removed
     */
    public val evictionCount: Long

    /**
     * Total operations that returned a `Failed`
     */
    public val failureCount: Long

    public companion object
}
