package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class DeviceFingerprint private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): DeviceFingerprint {
            require(value.isNotBlank()) { "deviceFingerprint must not be blank" }
            require(value.length <= 512) { "deviceFingerprint too long (max 512)" }
            return DeviceFingerprint(value)
        }
    }
}
