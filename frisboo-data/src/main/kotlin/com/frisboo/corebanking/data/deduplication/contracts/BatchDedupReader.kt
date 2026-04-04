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
 * Read contract for batch-level deduplication state.
 */
public interface BatchDedupReader {
    /**
     * Returns the current batch status.
     *
     * @param batchId batch identifier.
     * @return current status, or `null` when batch is unknown.
     */
    public suspend fun batchStatus(batchId: String): BatchStatus?

    /**
     * Checks whether an item was already processed in the given batch.
     *
     * @param batchId batch identifier.
     * @param itemId item identifier.
     * @return `true` when item is already processed.
     */
    public suspend fun isProcessed(
        batchId: String,
        itemId: String,
    ): Boolean
}
