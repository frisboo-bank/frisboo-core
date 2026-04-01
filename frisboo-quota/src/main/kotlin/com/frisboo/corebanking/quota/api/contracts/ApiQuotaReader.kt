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
package com.frisboo.corebanking.quota.api.contracts

import com.frisboo.corebanking.quota.api.model.ApiUsage

/**
 * Provides read-only access to API quota state.
 */
public interface ApiQuotaReader {
    /**
     * Returns usage snapshot for the given API key and endpoint.
     */
    public suspend fun usage(
        apiKey: String,
        endpoint: String,
    ): ApiUsage?

    /**
     * Returns remaining requests for the given API key and endpoint.
     */
    public suspend fun remainingRequests(
        apiKey: String,
        endpoint: String,
    ): Long
}
