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

import arrow.core.Either

/**
 * Atomic idempotency contract for check-and-mark semantics.
 */
public interface IdempotencyAtomic {
    /**
     * Atomically checks and marks a key as in-progress/processed.
     *
     * @param key idempotency key.
     * @return [Either.Right] `true` if newly marked, `false` if already present.
     */
    public suspend fun checkAndMark(key: String): Either<IdempotencyError, Boolean>
}
