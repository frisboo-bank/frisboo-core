/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.persistence.redis.constants

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public const val REDIS_DEFAULT_LOCK_SIZE: Int = 16
public const val REDIS_DEFAULT_MAX_SCAN_ITERATIONS: Long = 100_000L
public const val REDIS_DEFAULT_SCAN_BATCH_SIZE: Long = 1000L
public val REDIS_DEFAULT_LOCK_TTL: Duration = 30.seconds
public val REDIS_DEFAULT_OPERATION_TIMEOUT: Duration = 5.seconds


