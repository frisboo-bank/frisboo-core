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
package com.frisboo.corebanking.registry.models

import com.frisboo.corebanking.registry.errors.RegistryError

public sealed interface EvictResult {
    public data object Evicted : EvictResult

    public data object NotFound : EvictResult

    /**
     * Reserved for distributed implementations where eviction may fail due to
     * network or infrastructure errors (e.g., Redis connection timeout).
     */
    public data class Failed(
        public val error: RegistryError,
    ) : EvictResult
}
