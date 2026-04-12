package com.frisboo.corebanking.persistence.redis.constants

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public const val REDIS_IMAGE: String = "redis:8-alpine"
public const val REDIS_DEFAULT_SCAN_BATCH_SIZE: Long = 1000L
public const val REDIS_DEFAULT_PAGE_SIZE: Int = 1000
public val REDIS_DEFAULT_OPERATION_TIMEOUT: Duration = 30.seconds
public const val REDIS_DEFAULT_SCAN_MAX_RESPONSE_BYTES: Int = 10 * 1024 * 1024
