package com.frisboo.corebanking.persistence.redis.constants

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public const val REDIS_DEFAULT_PAGE_SIZE: Int = 1000
public const val REDIS_DEFAULT_SCAN_BATCH_SIZE: Long = 1000L
public val REDIS_DEFAULT_LOCK_TTL: Duration = 30.seconds
public val REDIS_DEFAULT_OPERATION_TIMEOUT: Duration = 30.seconds
public val REDIS_DEFAULT_WAIT_MAX_DELAY: Duration = 30.seconds
