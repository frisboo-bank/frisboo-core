package com.frisboo.corebanking.grpc.domain.dtos

import kotlin.time.Instant
import com.google.protobuf.Timestamp

public fun Instant.toProtoTimestamp(): Timestamp =
    Timestamp.newBuilder()
        .setSeconds(epochSeconds)
        .setNanos(nanosecondsOfSecond)
        .build()
