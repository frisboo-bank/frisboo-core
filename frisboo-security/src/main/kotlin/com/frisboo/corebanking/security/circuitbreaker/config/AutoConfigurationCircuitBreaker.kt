package com.frisboo.corebanking.security.circuitbreaker.config

import com.frisboo.corebanking.security.circuitbreaker.actuator.CircuitBreakerEndpoint
import com.frisboo.corebanking.security.circuitbreaker.adapter.resilience4j.Resilience4jCircuitBreakerFactory
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerFactory
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerInstrumentation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservationFactory
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerRegistry
import com.frisboo.corebanking.security.circuitbreaker.instrumentation.MicrometerCircuitBreakerInstrumentationImpl
import com.frisboo.corebanking.security.circuitbreaker.instrumentation.MicrometerCircuitBreakerObservationFactoryImpl
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerRegistryConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import com.frisboo.corebanking.security.circuitbreaker.registry.CircuitBreakerRegistryImpl
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock
import java.time.Duration

/**
 * Spring Boot auto-configuration for circuit breaker infrastructure.
 */
@Configuration
@EnableConfigurationProperties(CircuitBreakerProperties::class)
@ConditionalOnProperty(
    prefix = "frisboo.security.circuitbreaker",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@Import(AutoConfigurationCircuitBreaker.InstrumentationConfiguration::class)
public open class AutoConfigurationCircuitBreaker {

    @Bean
    @ConditionalOnMissingBean
    public open fun circuitBreakerClock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean
    public open fun circuitBreakerRegistryConfig(properties: CircuitBreakerProperties): CircuitBreakerRegistryConfig {
        return CircuitBreakerConfigResolver.resolveRegistry(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    public open fun globalCircuitBreakerConfig(properties: CircuitBreakerProperties): CircuitBreakerConfig {
        return CircuitBreakerConfigResolver.resolve(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    public open fun instanceCircuitBreakerConfigs(
        properties: CircuitBreakerProperties,
        globalConfig: CircuitBreakerConfig,
    ): Map<String, CircuitBreakerConfig> {
        return properties.instances.mapValues { (_, instance) ->
            CircuitBreakerConfigResolver.resolve(globalConfig, instance)
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public open fun resilience4jCircuitBreakerFactory(
        instrumentation: CircuitBreakerInstrumentation,
        observationFactory: CircuitBreakerObservationFactory,
    ): CircuitBreakerFactory {
        return Resilience4jCircuitBreakerFactory(instrumentation, observationFactory)
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean
    public open fun circuitBreakerRegistry(
        factory: CircuitBreakerFactory,
        registryConfig: CircuitBreakerRegistryConfig,
        globalConfig: CircuitBreakerConfig,
        instanceConfigs: Map<String, CircuitBreakerConfig>,
        clock: Clock,
        instrumentation: CircuitBreakerInstrumentation,
    ): CircuitBreakerRegistry {
        return CircuitBreakerRegistryImpl(
            factory,
            registryConfig,
            globalConfig,
            instanceConfigs,
            clock,
            instrumentation,
        )
    }

    @Configuration
    @ConditionalOnProperty(
        prefix = "frisboo.security.circuitbreaker",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    public open class InstrumentationConfiguration {

        @Bean
        @ConditionalOnClass(MeterRegistry::class)
        @ConditionalOnProperty(
            prefix = "frisboo.security.circuitbreaker.metrics",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true,
        )
        public open fun circuitBreakerInstrumentation(
            meterRegistry: MeterRegistry,
        ): CircuitBreakerInstrumentation {
            return MicrometerCircuitBreakerInstrumentationImpl(meterRegistry)
        }

        @Bean
        @ConditionalOnMissingBean(CircuitBreakerInstrumentation::class)
        public open fun noopCircuitBreakerInstrumentation(): CircuitBreakerInstrumentation =
            object : CircuitBreakerInstrumentation {
                override fun recordBreakerAccessed(name: String): Unit = Unit
                override fun recordDynamicBreakerCreated(name: String): Unit = Unit
                override fun recordDynamicBreakerEvicted(name: String): Unit = Unit
                override fun recordCallStarted(name: String): Unit = Unit
                override fun recordCallSuccess(name: String, duration: Duration): Unit = Unit
                override fun recordCallFailure(name: String, duration: Duration, throwable: Throwable): Unit = Unit
                override fun recordCallNotPermitted(
                    name: String,
                    duration: Duration,
                    state: CircuitBreakerState,
                ): Unit = Unit
                override fun recordStateTransition(
                    name: String,
                    fromState: CircuitBreakerState,
                    toState: CircuitBreakerState,
                ): Unit = Unit
                override fun recordRegistryCleanup(removedCount: Int, remainingCount: Int): Unit = Unit
                override fun revokeBreakerMetrics(name: String): Unit = Unit
            }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(ObservationRegistry::class)
        public open fun observationRegistry(): ObservationRegistry = ObservationRegistry.create()

        @Bean
        @ConditionalOnClass(ObservationRegistry::class)
        @ConditionalOnProperty(
            prefix = "frisboo.security.circuitbreaker",
            name = ["tracer.enabled"],
            havingValue = "true",
            matchIfMissing = true,
        )
        public open fun circuitBreakerObservationFactory(
            observationRegistry: ObservationRegistry,
        ): CircuitBreakerObservationFactory {
            return MicrometerCircuitBreakerObservationFactoryImpl(observationRegistry)
        }

        @Bean
        @ConditionalOnMissingBean(CircuitBreakerObservationFactory::class)
        public open fun noopCircuitBreakerObservationFactory(): CircuitBreakerObservationFactory =
            object : CircuitBreakerObservationFactory {
                override fun createObservation(breakerName: String): CircuitBreakerObservation =
                    object : CircuitBreakerObservation {
                        override fun start(): Unit = Unit
                        override fun recordSuccess(duration: Duration): Unit = Unit
                        override fun recordFailure(duration: Duration, throwable: Throwable): Unit = Unit
                        override fun recordNotPermitted(duration: Duration, state: CircuitBreakerState): Unit = Unit
                        override fun close(): Unit = Unit
                    }
            }
    }

    @Configuration
    @AutoConfigureAfter(AutoConfigurationCircuitBreaker::class)
    @ConditionalOnProperty(
        prefix = "frisboo.security.circuitbreaker",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    public open class AutoConfigurationCircuitBreakerActuator {

        @Bean
        @ConditionalOnClass(Endpoint::class)
        @ConditionalOnProperty(
            prefix = "frisboo.security.circuitbreaker.actuator",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = false,
        )
        public open fun circuitBreakerEndpoint(registry: CircuitBreakerRegistry): CircuitBreakerEndpoint {
            return CircuitBreakerEndpoint(registry)
        }
    }
}
