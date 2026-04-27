package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class IdempotencyKey private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): IdempotencyKey {
            require(value.isNotBlank()) { "idempotencyKey must not be blank" }
            require(value.length <= 128) { "idempotencyKey too long (max 128)" }
            return IdempotencyKey(value)
        }
    }
}
