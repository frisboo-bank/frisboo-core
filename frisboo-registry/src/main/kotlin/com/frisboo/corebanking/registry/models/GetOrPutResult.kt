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

/**
 * Outcome of a [com.frisboo.corebanking.registry.contracts.RegistryWriter.getOrPut] operation.
 *
 * Returns the value together with an explicit signal of whether the factory was invoked,
 * enabling accurate audit trails without racy pre-checks.
 */
public sealed interface GetOrPutResult<out V> {
    /** The value returned (either from the store or from the factory). */
    public val value: V

    /** The factory was invoked and a new entry was stored. */
    public data class Created<V>(
        override val value: V,
    ) : GetOrPutResult<V>

    /** An existing non-expired entry was found; the factory was not invoked. */
    public data class Found<V>(
        override val value: V,
    ) : GetOrPutResult<V>

    /** The operation failed due to invalid input (e.g. non-positive TTL). */
    public data class Failed(
        public val error: RegistryError,
    ) : GetOrPutResult<Nothing> {
        override val value: Nothing
            get() = throw IllegalStateException("GetOrPutResult.Failed has no value: $error")
    }
}
