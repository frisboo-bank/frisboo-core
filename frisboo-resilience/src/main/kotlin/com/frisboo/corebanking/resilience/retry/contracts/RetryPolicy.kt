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
package com.frisboo.corebanking.resilience.retry.contracts

import com.frisboo.corebanking.resilience.retry.model.RetryDecision

/**
 * Policy that determines whether a failed operation should be retried.
 *
 * Implementations are stateless decision functions; retry state (attempt count, etc.)
 * is passed in via [attempt] and [error].
 */
public interface RetryPolicy {
    /**
     * Evaluates whether the operation should be retried.
     *
     * @param attempt the zero-based attempt number (0 = first failure).
     * @param error the error that caused the failure.
     * @return [RetryDecision.Retry] with a delay, or [RetryDecision.Stop] to give up.
     */
    public fun shouldRetry(
        attempt: Int,
        error: Throwable,
    ): RetryDecision
}
