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
 * Executes an operation with a fallback strategy (e.g. circuit-breaking, retry).
 *
 * **Cancellation contract**: Implementations **must** rethrow [kotlin.coroutines.cancellation.CancellationException]
 * and must **never** catch it as a recoverable failure. Structured concurrency depends on this.
 */
public interface StateManagerResilienceExecutor {

    /**
     * Runs [block]; if it fails, runs [fallback].
     *
     * @throws kotlin.coroutines.cancellation.CancellationException always rethrown, never swallowed
     */
    public suspend fun <T> execute(
        block: suspend () -> T,
        fallback: suspend () -> T,
    ): T
}
