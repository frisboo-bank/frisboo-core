package com.frisboo.corebanking.core.repositories

/**
 * Context class for managing locked transactions.
 */
public class LockedTransactionContext {
    public companion object {
        public fun create(): LockedTransactionContext = LockedTransactionContext()
    }
}
