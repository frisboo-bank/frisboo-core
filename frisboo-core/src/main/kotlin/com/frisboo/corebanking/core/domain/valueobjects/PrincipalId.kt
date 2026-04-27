package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class PrincipalId private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): PrincipalId {
            require(value.isNotBlank()) { "principalId must not be blank" }
            require(value.length <= 128) { "principalId too long (max 128)" }
            return PrincipalId(value)
        }
    }
}
