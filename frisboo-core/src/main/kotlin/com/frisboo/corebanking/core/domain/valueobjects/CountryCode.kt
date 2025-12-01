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
package com.frisboo.corebanking.customerservice.domain.customer.valueobjects

import java.util.Locale

@JvmInline
public value class CountryCode private constructor(
    public val code: String,
) {
    public companion object {
        public fun of(code: String): CountryCode {
            require(code.length == 2) { "Country code must be exactly 2 characters long: `$code`" }
            require(code.all { it.isLetter() }) { "Country code must contain only letters: `$code`" }

            return CountryCode(code.uppercase(Locale.ROOT))
        }
    }
}
