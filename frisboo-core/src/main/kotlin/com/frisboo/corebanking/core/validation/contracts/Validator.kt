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
package com.frisboo.corebanking.core.validation.contracts

import arrow.core.Either
import com.frisboo.corebanking.core.domain.errors.AppError
import com.frisboo.corebanking.core.validation.model.ValidationResult

/**
 * Contract for validating a value of type [T] by composing multiple [ValidationRule]s.
 *
 * Returns [Either.Right] with the validated value on success,
 * or [Either.Left] with an [AppError] containing all collected violations on failure.
 * This bridges the validation subsystem into the project's sealed-error convention.
 *
 * @param T the type of value being validated.
 */
public interface Validator<in T> {
    public fun validate(value: T): Either<AppError, ValidationResult.Valid>
}
