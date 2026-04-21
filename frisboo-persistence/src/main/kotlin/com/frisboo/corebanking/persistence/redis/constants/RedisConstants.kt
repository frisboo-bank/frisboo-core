package com.frisboo.corebanking.persistence.redis.constants

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public const val REDIS_DEFAULT_LOCK_SIZE: Int = 16
public const val REDIS_DEFAULT_MAX_SCAN_ITERATIONS: Long = 100_000L
public const val REDIS_DEFAULT_SCAN_BATCH_SIZE: Long = 1000L
public val REDIS_DEFAULT_LOCK_TTL: Duration = 30.seconds
public val REDIS_DEFAULT_OPERATION_TIMEOUT: Duration = 5.seconds
