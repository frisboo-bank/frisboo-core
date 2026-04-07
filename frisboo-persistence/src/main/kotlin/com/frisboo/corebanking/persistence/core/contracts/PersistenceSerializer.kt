package com.frisboo.corebanking.persistence.core.contracts

public interface PersistenceSerializer<T> {
    public fun serialize(value: T): ByteArray
    public fun deserialize(value: ByteArray): T
}

