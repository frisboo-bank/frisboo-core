package com.frisboo.corebanking.core.domain.valueobjects

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
public value class CustomerGuid private constructor(public val value: Uuid) {
    public companion object {
        public fun generate(): CustomerGuid = CustomerGuid(Uuid.random())
        public fun of(value: Uuid): CustomerGuid = CustomerGuid(value)
        public fun of(value: String): CustomerGuid {
            val uuid = try {
                Uuid.parse(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("customerGuid must be a valid UUID: $value")
            }
            return CustomerGuid(uuid)
        }
    }
}
