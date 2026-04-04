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
package com.frisboo.corebanking.core.validation.model

/**
 * Result of applying one or more [ValidationRule][com.frisboo.corebanking.core.validation.contracts.ValidationRule]s.
 *
 * Collects all violations rather than failing on the first one,
 * enabling API responses that report every invalid field at once.
 */
public sealed interface ValidationResult {
    public data object Valid : ValidationResult

    public data class Invalid(
        val violations: List<Violation>,
    ) : ValidationResult {
        init {
            require(violations.isNotEmpty()) { "Invalid result must contain at least one violation" }
        }
    }
}

public data class Violation(
    val field: String,
    val message: String,
)
