package com.frisboo.corebanking.core.domain.valueobjects

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
public value class CorrelationId private constructor(public val value: Uuid) {
    public companion object {
        public fun generate(): CorrelationId = CorrelationId(Uuid.random())

        public fun of(value: Uuid): CorrelationId = CorrelationId(value)
        public fun of(value: String): CorrelationId {
            val uuid = try {
                Uuid.parse(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("correlationId must be a valid UUID: $value")
            }
            return CorrelationId(uuid)
        }
    }
}
