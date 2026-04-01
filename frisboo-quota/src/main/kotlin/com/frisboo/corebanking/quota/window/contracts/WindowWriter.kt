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
package com.frisboo.corebanking.quota.window.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Describes failures that can occur while mutating window state.
 */
public sealed interface WindowError {
    /**
     * Indicates the underlying window storage is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : WindowError
}

/**
 * Provides write operations for sliding window counters.
 */
public interface WindowWriter {
    /**
     * Records a usage event at current timestamp.
     */
    public suspend fun record(windowId: String): Either<WindowError, Unit>

    /**
     * Removes records older than [olderThan] and returns trimmed count.
     */
    public suspend fun trim(
        windowId: String,
        olderThan: Duration,
    ): Either<WindowError, Int>
}
