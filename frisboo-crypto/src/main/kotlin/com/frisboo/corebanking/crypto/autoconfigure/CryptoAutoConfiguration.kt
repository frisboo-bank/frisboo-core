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
package com.frisboo.corebanking.crypto.autoconfigure

import com.frisboo.corebanking.crypto.adapters.tink.TinkCryptoInitializer
import com.frisboo.corebanking.crypto.adapters.tink.TinkCryptoService
import com.frisboo.corebanking.crypto.adapters.tink.TinkHybridCryptoService
import com.frisboo.corebanking.crypto.contracts.CryptoService
import com.frisboo.corebanking.crypto.contracts.HybridCryptoService
import com.google.crypto.tink.KeysetHandle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configures crypto services when enabled via [CryptoProperties].
 *
 * The consumer must provide [KeysetHandle] beans loaded from KMS:
 *
 * ```kotlin
 * @Bean @Qualifier("aeadKeysetHandle")
 * fun aeadKeysetHandle(): KeysetHandle = // load from KMS
 *
 * @Bean @Qualifier("hybridKeysetHandle")
 * fun hybridKeysetHandle(): KeysetHandle = // load from KMS
 * ```
 *
 * If you define your own [CryptoService] or [HybridCryptoService] bean, the auto-configured one backs off.
 *
 * Note: This class is `open` (not `final`) because Spring Boot CGLIB proxying requires subclassing
 * for `@Bean` method interception inside `@AutoConfiguration` classes.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "frisboo.corebanking.crypto", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(CryptoProperties::class)
public open class CryptoAutoConfiguration {
    init {
        TinkCryptoInitializer.initialize()
    }

    @Bean
    @ConditionalOnProperty(prefix = "frisboo.corebanking.crypto.aead", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(name = ["aeadKeysetHandle"])
    @ConditionalOnMissingBean(CryptoService::class)
    public open fun cryptoService(
        @Qualifier("aeadKeysetHandle") aeadKeysetHandle: KeysetHandle,
    ): CryptoService = TinkCryptoService(aeadKeysetHandle)

    @Bean
    @ConditionalOnProperty(prefix = "frisboo.corebanking.crypto.hybrid", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(name = ["hybridKeysetHandle"])
    @ConditionalOnMissingBean(HybridCryptoService::class)
    public open fun hybridCryptoService(
        @Qualifier("hybridKeysetHandle") hybridKeysetHandle: KeysetHandle,
    ): HybridCryptoService = TinkHybridCryptoService(hybridKeysetHandle)
}
