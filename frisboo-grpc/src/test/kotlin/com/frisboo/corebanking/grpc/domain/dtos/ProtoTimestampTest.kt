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
package com.frisboo.corebanking.grpc.domain.dtos

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import kotlin.time.Instant

class ProtoTimestampTest :
    StringSpec(
        {
            "it should convert Instant to Proto Timestamp and back correctly" {
                checkAll<Instant> { instant ->
                    val protoTimestamp = instant.toProtoTimestamp()
                    val convertedInstant = protoTimestamp.toInstant()

                    protoTimestamp shouldNotBe null
                    convertedInstant shouldBe instant
                }
            }
        },
    )
