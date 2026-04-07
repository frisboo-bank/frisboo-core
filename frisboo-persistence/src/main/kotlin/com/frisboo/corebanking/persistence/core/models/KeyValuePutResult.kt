package com.frisboo.corebanking.persistence.core.models

/**
 * Outcome of a `put` operation on a key‑value store.
 */
public sealed interface KeyValuePutResult {
    public data object Created : KeyValuePutResult
    public data object Updated : KeyValuePutResult
}
