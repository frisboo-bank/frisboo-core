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
package com.frisboo.corebanking.core.factory.decorators

import com.frisboo.corebanking.core.factory.contracts.AsyncFactory

public class MetricsFactory<C : Any, T : Any>(
    private val delegate: AsyncFactory<C, T>,
) : AsyncFactory<C, T> {
    override suspend fun getOrCreate(name: String, config: C): T = delegate.getOrCreate(name, config)

    override suspend fun evict(name: String): Unit = delegate.evict(name)

    override suspend fun estimatedSize(): Long = delegate.estimatedSize()
}
