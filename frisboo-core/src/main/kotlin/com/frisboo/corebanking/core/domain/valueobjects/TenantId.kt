package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class TenantId private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): TenantId {
            require(value.isNotBlank()) { "tenantId must not be blank" }
            require(value.length <= 64) { "tenantId too long (max 64)" }
            return TenantId(value)
        }
    }
}
