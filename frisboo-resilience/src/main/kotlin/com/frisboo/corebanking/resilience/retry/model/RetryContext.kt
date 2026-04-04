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
package com.frisboo.corebanking.resilience.retry.model

import kotlin.time.Duration

/**
 * Context provided to retry decision logic.
 *
 * @property attempt current attempt number.
 * @property lastError most recent failure cause, if any.
 * @property totalElapsed total elapsed time since first attempt.
 */
public data class RetryContext(
    public val attempt: Int,
    public val lastError: Throwable?,
    public val totalElapsed: Duration,
)
