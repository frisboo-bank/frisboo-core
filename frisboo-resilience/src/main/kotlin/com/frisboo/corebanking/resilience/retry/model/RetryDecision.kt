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
package com.frisboo.corebanking.resilience.retry.model

import java.time.Duration

/**
 * The outcome of evaluating a retry policy.
 */
public sealed interface RetryDecision {
    /** Retry the operation after the specified [delay]. */
    public data class Retry(
        val delay: Duration,
    ) : RetryDecision

    /** Stop retrying. The operation has exhausted its retry budget. */
    public data object Stop : RetryDecision
}
