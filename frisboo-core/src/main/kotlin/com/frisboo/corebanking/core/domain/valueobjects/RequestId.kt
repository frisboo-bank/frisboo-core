package com.frisboo.corebanking.core.domain.valueobjects

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@OptIn(ExperimentalUuidApi::class)
public value class RequestId private constructor(public val value: Uuid) {
    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public fun generate(): RequestId = RequestId(Uuid.random())

        public fun of(value: Uuid): RequestId = RequestId(value)
        public fun of(value: String): RequestId {
            val uuid = try {
                Uuid.parse(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("requestId must be a valid UUID: $value")
            }
            return RequestId(uuid)
        }
    }
}
