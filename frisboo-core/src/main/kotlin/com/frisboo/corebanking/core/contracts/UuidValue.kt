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
package com.frisboo.corebanking.core.contracts

import java.util.UUID

public interface UUIDValue {
    public val id: UUID?

    public fun string(): String = id?.toString() ?: ""
}

public interface UUIDCompanion<T>

public fun <T> UUIDCompanion<T>.create(constructor: (UUID) -> T): T = constructor(UUID.randomUUID())

public fun <T> UUIDCompanion<T>.fromString(
    id: String?,
    constructor: (UUID?) -> T,
): T = constructor(id?.let { UUID.fromString(it) })
