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
package com.frisboo.corebanking.statemanager.contracts

/**
 * Composite statemanager interface combining read, write, and admin operations.
 *
 * All operations are scoped to a [com.frisboo.corebanking.persistence.core.models.StateManagerScope]
 * (team + name) ensuring strict multi-team isolation.
 *
 * Consumers should depend on the narrowest sub-interface they need
 * ([StateManagerReader], [StateManagerWriter], or [StateManagerAdmin]).
 */
public interface StateManager<K : Any, V : Any> :
    StateManagerReader<K, V>,
    StateManagerWriter<K, V>,
    StateManagerAdmin<K, V>
