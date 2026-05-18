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
package com.frisboo.corebanking.statemanager.models

import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class StateManagerScopeTest : StringSpec() {
    init {
        "accepts single lowercase letter" {
            shouldNotThrow<IllegalArgumentException> {
                StateManagerScope(name = "a", team = "t")
            }
        }

        "accepts two-char identifier" {
            shouldNotThrow<IllegalArgumentException> {
                StateManagerScope(name = "ab", team = "t1")
            }
        }

        "accepts max-length 63-char identifier" {
            val maxName = "a" + "b".repeat(61) + "c"
            shouldNotThrow<IllegalArgumentException> {
                StateManagerScope(name = maxName, team = "t")
            }
        }

        "accepts hyphens in middle" {
            shouldNotThrow<IllegalArgumentException> {
                StateManagerScope(name = "my-scope", team = "my-team")
            }
        }

        "accepts digits in middle and end" {
            shouldNotThrow<IllegalArgumentException> {
                StateManagerScope(name = "scope1", team = "team2")
            }
        }

        "rejects empty name" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "", team = "team")
            }
        }

        "rejects name starting with digit" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "1scope", team = "team")
            }
        }

        "rejects name starting with hyphen" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "-scope", team = "team")
            }
        }

        "rejects name ending with hyphen" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "scope-", team = "team")
            }
        }

        "rejects uppercase letters" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "Scope", team = "team")
            }
        }

        "rejects 64-char identifier (exceeds max)" {
            val tooLong = "a" + "b".repeat(62) + "c"
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = tooLong, team = "team")
            }
        }

        "rejects special characters (colon for Redis injection)" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "scope:inject", team = "team")
            }
        }

        "rejects special characters (asterisk for Redis glob)" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "scope*", team = "team")
            }
        }

        "rejects underscore" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "my_scope", team = "team")
            }
        }

        "team validation uses same rules as name" {
            shouldThrow<IllegalArgumentException> {
                StateManagerScope(name = "valid", team = "INVALID")
            }
        }

        "prefix returns team:name" {
            val scope = StateManagerScope(name = "cache", team = "payments")
            scope.prefix shouldBe "payments:cache"
        }
    }
}
