package com.frisboo.corebanking.core.factory.models

import kotlin.time.Duration

public data class AsyncFactoryCachingConfig<C : Any, T : Any>(
    val enabled: Boolean = true,
    val maxSize: Long,
    val expireAfterAccess: Duration,
    val matchesConfig: (T, C) -> Boolean,
    val recordStats: Boolean = true,
)
