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
package com.frisboo.corebanking.registry.adapters.local.caffeine

import com.github.benmanes.caffeine.cache.Expiry

internal class PerEntryExpiry<K : Any, V : Any> : Expiry<K, TimedValue<V>> {
    override fun expireAfterCreate(
        key: K,
        value: TimedValue<V>,
        currentTime: Long,
    ): Long = value.ttlNanos

    override fun expireAfterUpdate(
        key: K,
        value: TimedValue<V>,
        currentTime: Long,
        currentDuration: Long,
    ): Long = value.ttlNanos

    override fun expireAfterRead(
        key: K,
        value: TimedValue<V>,
        currentTime: Long,
        currentDuration: Long,
    ): Long = currentDuration
}
