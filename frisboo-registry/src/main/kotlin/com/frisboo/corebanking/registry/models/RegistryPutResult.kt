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

public sealed interface RegistryPutResult<out V> {

    public val previousValue: V?
    public val newValue: V?

    /**
     * Represents a successful creation of a new value in the registry.
     *
     * @param newValue The value that has been associated with the key after the creation.
     */
    public data class Created<V>(
        override val newValue: V,
    ) : RegistryPutResult<V> {
        override val previousValue: V? = null
    }

    /**
     * Represents a successful update of an existing value in the registry.
     *
     * @param previousValue The value that was previously associated with the key before the update.
     * @param newValue The new value that has been associated with the key after the update.
     */
    public data class Updated<V>(
        override val previousValue: V,
        override val newValue: V,
    ) : RegistryPutResult<V>
}
