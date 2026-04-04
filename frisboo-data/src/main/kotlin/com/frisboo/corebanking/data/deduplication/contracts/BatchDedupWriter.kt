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

import arrow.core.Either

/**
 * Write contract for batch-level deduplication state.
 */
public interface BatchDedupWriter {
    /**
     * Registers a new batch with its item identifiers.
     *
     * @param batchId batch identifier.
     * @param itemIds item identifiers to track.
     * @return operation result.
     */
    public suspend fun startBatch(
        batchId: String,
        itemIds: List<String>,
    ): Either<DeduplicationError, Unit>

    /**
     * Marks an item as processed in a batch.
     *
     * @param batchId batch identifier.
     * @param itemId item identifier.
     * @return operation result.
     */
    public suspend fun markItemProcessed(
        batchId: String,
        itemId: String,
    ): Either<DeduplicationError, Unit>

    /**
     * Marks the batch as completed.
     *
     * @param batchId batch identifier.
     * @return operation result.
     */
    public suspend fun completeBatch(batchId: String): Either<DeduplicationError, Unit>
}
