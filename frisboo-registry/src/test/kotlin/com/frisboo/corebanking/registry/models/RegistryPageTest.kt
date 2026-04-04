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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class RegistryPageTest :
    StringSpec({
        val maxPageSize = 100

        "returns first page with null cursor" {
            val page = buildPage(listOf("a", "b", "c"), cursor = null, limit = 2, maxPageSize = maxPageSize)
            page.items shouldBe listOf("a", "b")
            page.nextCursor shouldBe "2"
        }

        "returns subsequent page with valid cursor" {
            val page = buildPage(listOf("a", "b", "c"), cursor = "2", limit = 10, maxPageSize = maxPageSize)
            page.items shouldBe listOf("c")
            page.nextCursor shouldBe null
        }

        "clamps limit to 1 when zero or negative" {
            val page = buildPage(listOf("a", "b", "c"), cursor = null, limit = 0, maxPageSize = maxPageSize)
            page.items shouldBe listOf("a")
            page.nextCursor shouldBe "1"
        }

        "clamps limit to maxPageSize when too large" {
            val page = buildPage(listOf("a", "b", "c"), cursor = null, limit = 9999, maxPageSize = 2)
            page.items shouldBe listOf("a", "b")
            page.nextCursor shouldBe "2"
        }

        "returns empty page when cursor is at end" {
            val page = buildPage(listOf("a", "b"), cursor = "2", limit = 10, maxPageSize = maxPageSize)
            page.items shouldBe emptyList()
            page.nextCursor shouldBe null
        }

        "returns empty page when cursor is beyond end" {
            val page = buildPage(listOf("a", "b"), cursor = "999", limit = 10, maxPageSize = maxPageSize)
            page.items shouldBe emptyList()
            page.nextCursor shouldBe null
        }

        "throws on non-numeric cursor" {
            shouldThrow<IllegalArgumentException> {
                buildPage(listOf("a"), cursor = "abc", limit = 10, maxPageSize = maxPageSize)
            }
        }

        "throws on negative cursor" {
            shouldThrow<IllegalArgumentException> {
                buildPage(listOf("a"), cursor = "-1", limit = 10, maxPageSize = maxPageSize)
            }
        }

        "returns empty page for empty list" {
            val page = buildPage(emptyList<String>(), cursor = null, limit = 10, maxPageSize = maxPageSize)
            page.items shouldBe emptyList()
            page.nextCursor shouldBe null
        }

        "limit of 1 pages correctly" {
            val all = listOf("a", "b", "c")
            val p1 = buildPage(all, cursor = null, limit = 1, maxPageSize = maxPageSize)
            p1.items shouldBe listOf("a")
            p1.nextCursor shouldBe "1"

            val p2 = buildPage(all, cursor = "1", limit = 1, maxPageSize = maxPageSize)
            p2.items shouldBe listOf("b")
            p2.nextCursor shouldBe "2"

            val p3 = buildPage(all, cursor = "2", limit = 1, maxPageSize = maxPageSize)
            p3.items shouldBe listOf("c")
            p3.nextCursor shouldBe null
        }
    })
