package com.frisboo.corebanking.registry.adapters.distributed.redis

import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.registry.errors.RegistryError

internal fun PersistenceError.toRegistryError(): RegistryError = when (this) {
    is PersistenceError.ConnectionFailed ->
        RegistryError.ConnectionFailed(message, cause)

    is PersistenceError.OperationTimeout ->
        RegistryError.OperationTimeout(message, timeoutMillis)

    is PersistenceError.InvalidArgument ->
        RegistryError.InvalidArgument(name, message)

    is PersistenceError.SerializationFailed ->
        RegistryError.SerializationFailed(message, cause)

    is PersistenceError.DeserializationFailed ->
        RegistryError.DeserializationFailed(message, cause)

    else -> RegistryError.ConnectionFailed("Unexpected persistence error: $this")
}
