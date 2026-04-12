package com.frisboo.corebanking.registry.serialization

import com.frisboo.corebanking.registry.contracts.RegistrySerializer

public class StringRegistrySerializer : RegistrySerializer<String> {

    override fun serialize(value: String): ByteArray = value.toByteArray(Charsets.UTF_8)

    override fun deserialize(value: ByteArray): String = value.toString(Charsets.UTF_8)
}
