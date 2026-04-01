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
package com.frisboo.corebanking.core.domain.errors

public interface AppError

public data class GenericAppError(
    val message: String,
    val cause: Throwable? = null,
    val exceptionClazz: Class<*>? = null,
) : AppError

public data class InvalidVersion(
    val msg: String,
) : AppError

public sealed class EventVersionError(
    public open val id: Any?,
    public open val expectedVersion: Any,
    public open val eventVersion: Any,
) : AppError {
    public data class Lower(
        override val id: Any?,
        override val expectedVersion: Any,
        override val eventVersion: Any,
    ) : EventVersionError(id, expectedVersion, eventVersion)

    public data class Same(
        override val id: Any?,
        override val expectedVersion: Any,
        override val eventVersion: Any,
    ) : EventVersionError(id, expectedVersion, eventVersion)

    public data class Upper(
        override val id: Any?,
        override val expectedVersion: Any,
        override val eventVersion: Any,
    ) : EventVersionError(id, expectedVersion, eventVersion)
}

public data class InvalidTransactionError(
    val msg: String,
    val transactionId: String = "",
) : AppError

public data class SerializationError(
    val msg: String,
) : AppError
