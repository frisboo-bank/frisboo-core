package com.frisboo.corebanking.observability.audit.models

@JvmInline
public value class ObservabilityAuditBaggage internal constructor(
    private val entries: Map<String, String>,
) {
    public operator fun get(key: String): String? = entries[key]

    public fun toMap(): Map<String, String> = entries.toMap()

    public companion object {
        public fun from(map: Map<String, String>): ObservabilityAuditBaggage =
            ObservabilityAuditBaggage(map.toMap())
    }
}
