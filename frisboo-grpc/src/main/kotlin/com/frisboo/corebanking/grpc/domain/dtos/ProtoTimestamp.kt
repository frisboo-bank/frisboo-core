package com.frisboo.corebanking.grpc.domain.dtos

import kotlin.time.Instant
import com.google.protobuf.Timestamp

public fun Timestamp.toInstant(): Instant =
    Instant.fromEpochSeconds(seconds, nanos)
