package com.frisboo.corebanking.persistence.core.serializers

import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer

public class PersistenceStringSerializerImpl : PersistenceSerializer<String> {
    override fun serialize(value: String): ByteArray = value.toByteArray(Charsets.UTF_8)
    override fun deserialize(value: ByteArray): String = String(value)
}
