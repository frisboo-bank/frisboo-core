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
package com.frisboo.corebanking.registry.contracts

/**
 * Composite registry interface combining read, write, and admin operations.
 *
 * All operations are scoped to a [com.frisboo.corebanking.registry.models.RegistryScope]
 * (team + name) ensuring strict multi-team isolation.
 *
 * Consumers should depend on the narrowest sub-interface they need
 * ([RegistryReader], [RegistryWriter], or [RegistryAdmin]).
 */
public interface Registry<K : Any, V : Any> :
    RegistryReader<K, V>,
    RegistryWriter<K, V>,
    RegistryAdmin<K, V>
