package com.frisboo.corebanking.grpc.domain.dtos

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import kotlin.time.Instant

class ProtoTimestampTest : StringSpec(
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
