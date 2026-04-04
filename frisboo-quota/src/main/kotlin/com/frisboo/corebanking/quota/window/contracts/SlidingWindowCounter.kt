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

/**
 * Aggregate sliding window facade combining read and write operations.
 */
public interface SlidingWindowCounter :
    WindowReader,
    WindowWriter {
    /**
     * Returns true when current count is less than or equal to [limit].
     */
    public suspend fun isWithinLimit(
        windowId: String,
        limit: Long,
    ): Boolean
}
