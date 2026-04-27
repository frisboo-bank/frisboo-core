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
package com.frisboo.corebanking.persistence.redis.models

@JvmInline
public value class RedisScanCursor internal constructor(
    public val value: Long,
) {
    public val cursor: Long get() = value and FINISHED_BIT.inv()
    public val isFinished: Boolean get() = (value and FINISHED_BIT) != 0L

    public companion object {
        private const val FINISHED_BIT: Long = 1L shl 63

        public val INITIAL: RedisScanCursor = RedisScanCursor(0L)
        public val FINISHED: RedisScanCursor = RedisScanCursor(FINISHED_BIT)

        public fun of(cursor: String?): RedisScanCursor = when {
            cursor.isNullOrBlank() -> INITIAL
            cursor == "0" -> FINISHED
            else -> try {
                RedisScanCursor(cursor.toLong())
            } catch (_: NumberFormatException) {
                INITIAL
            }
        }
    }
}

