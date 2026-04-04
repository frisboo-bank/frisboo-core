package com.frisboo.corebanking.core.factory.contracts

/**
 * Factory that creates named instances with a specific configuration type.
 *
 * @param C the configuration type for creating instances
 * @param T the type of instance created
 */
public interface AsyncFactory<C : Any, T : Any> {

    /**
     * Returns an existing instance for [name] with the given [config], or creates a new one.
     * Implementations must be idempotent for the same (name, config).
     */
    public suspend fun getOrCreate(name: String, config: C): T

    /** Evicts the instance for [name] from the factory's cache (if any). */
    public suspend fun evict(name: String)

    /** Approximate number of cached instances (0 if no caching). */
    public suspend fun estimatedSize(): Long
}
