package com.frisboo.corebanking.persistence.core.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.core.models.KeyValuePutResult
import kotlin.time.Duration

/**
 * Generic key‑value store contract.
 * Implementations may be Redis, in‑memory, or other backends.
 */
public interface KeyValueStore<K : Any, V : Any> {

    /**
     * Retrieves the value associated with [key], or `null` if absent.
     */
    public suspend fun get(key: K): Either<PersistenceError, V?>

    /**
     * Stores [value] under [key] with an optional [ttl].
     *
     * @return [KeyValuePutResult.Created] if the key was new,
     *         [KeyValuePutResult.Updated] if an existing value was overwritten.
     */
    public suspend fun put(
        key: K,
        value: V,
        ttl: Duration? = null,
    ): Either<PersistenceError, KeyValuePutResult>

    /**
     * Atomically retrieves the value for [key] if present; otherwise invokes [factory],
     * stores the result with an optional [ttl], and returns it.
     */
    public suspend fun getOrPut(
        key: K,
        ttl: Duration? = null,
        factory: suspend () -> V,
    ): Either<PersistenceError, V>

    /**
     * Removes the entry for [key].
     *
     * @return `true` if an entry was removed, `false` otherwise.
     */
    public suspend fun evict(key: K): Either<PersistenceError, Boolean>

    /**
     * Checks whether an entry exists for [key].
     */
    public suspend fun contains(key: K): Either<PersistenceError, Boolean>
}
