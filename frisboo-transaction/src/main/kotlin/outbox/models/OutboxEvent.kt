package outbox.models

import java.util.UUID
import java.time.Instant

public data class OutboxEvent(
    val eventId: UUID,
    val name: String,
    val subject: String,
    val data: ByteArray,
    val metadata: ByteArray,
    val sentAt: Instant,
    val publishedAt: Instant? = null,
    val version: Long? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    public companion object {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutboxEvent

        if (version != other.version) return false
        if (eventId != other.eventId) return false
        if (name != other.name) return false
        if (subject != other.subject) return false
        if (!data.contentEquals(other.data)) return false
        if (!metadata.contentEquals(other.metadata)) return false
        if (sentAt != other.sentAt) return false
        if (publishedAt != other.publishedAt) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version?.hashCode() ?: 0
        result = 31 * result + eventId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + metadata.contentHashCode()
        result = 31 * result + sentAt.hashCode()
        result = 31 * result + (publishedAt?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }
}
