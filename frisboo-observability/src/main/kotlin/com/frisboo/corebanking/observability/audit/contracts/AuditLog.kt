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
package com.frisboo.corebanking.observability.audit.contracts

import arrow.core.Either
import com.frisboo.corebanking.observability.audit.model.AuditEntry

/**
 * Contract for persisting audit trail entries.
 *
 * Implementations must guarantee durability: once [record] returns [Either.Right],
 * the entry is persisted to a tamper-evident store (append-only database, immutable log, etc.).
 */
public interface AuditLog {
    public suspend fun record(entry: AuditEntry): Either<AuditLogError, Unit>
}

public sealed interface AuditLogError {
    public data class Unavailable(
        val cause: Throwable? = null,
    ) : AuditLogError

    public data class SerializationFailure(
        val message: String,
    ) : AuditLogError
}
