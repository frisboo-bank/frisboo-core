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

import arrow.core.Either

/**
 * Named registry contract for retry policies.
 */
public interface RetryPolicyStore {
    /**
     * Retrieves a registered retry policy by [name].
     */
    public suspend fun get(name: String): RetryPolicy?

    /**
     * Registers [policy] under [name].
     */
    public suspend fun register(
        name: String,
        policy: RetryPolicy,
    ): Either<RetryError, Unit>
}
