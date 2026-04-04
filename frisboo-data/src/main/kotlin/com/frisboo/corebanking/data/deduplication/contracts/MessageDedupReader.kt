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
package com.frisboo.corebanking.data.deduplication.contracts

/**
 * Read contract for message deduplication state.
 */
public interface MessageDedupReader {
    /**
     * Checks whether a message key is already registered as duplicate.
     *
     * @param key message deduplication key.
     * @return `true` when key has already been seen.
     */
    public suspend fun isDuplicate(key: String): Boolean

    /**
     * Counts all currently tracked duplicate keys.
     *
     * @return number of tracked keys.
     */
    public suspend fun duplicateCount(): Long
}
