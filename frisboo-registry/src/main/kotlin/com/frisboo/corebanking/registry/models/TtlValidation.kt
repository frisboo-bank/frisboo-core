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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * Validates that [ttl] is positive (> 0). Returns null if valid, or [RegistryError.InvalidTtl] if not.
 */
internal fun validateTtl(ttl: Duration): RegistryError.InvalidTtl? =
    if (ttl <= ZERO) RegistryError.InvalidTtl(message = "TTL must be positive, got: $ttl") else null

/**
 * Validates optional [ttl] for write operations. Returns null if valid (including null ttl),
 * or [RegistryError.InvalidTtl] if ttl is non-null and not positive.
 */
internal fun validateOptionalTtl(ttl: Duration?): RegistryError.InvalidTtl? =
    if (ttl != null) validateTtl(ttl) else null
