# frisboo-resilience

Protect downstream services from cascading failures with circuit breakers and rate limiters

## Circuit Breaker

### What does it do?

When a downstream service starts failing, a circuit breaker **stops calling it** for a cooldown period instead of hammering it with requests that will also fail. After the cooldown, it lets a few trial calls through to check if the service has recovered.

| State | What happens |
|-------|--------------|
| **CLOSED** | All calls pass through normally. Failures are tracked in a sliding window. |
| **OPEN** | All calls are rejected immediately — `executeSuspend` returns `CircuitBreakerResult.Rejected`. No network call is made. |
| **HALF_OPEN** | A limited number of trial calls are allowed through. If they succeed, the circuit closes. If they fail, it opens again. |

### Getting started

#### 1. Create a circuit breaker

```kotlin
val factory: CircuitBreakerFactory = CircuitBreakerFactoryImpl(
    stateStateManager = stateStateManager,       // persists state across restarts
    coroutineScope = coroutineScope,     // scope for background state persistence
)

val breaker = factory.create(
    name = "payment-gateway",
    config = CircuitBreakerConfig(
        failureRateThreshold = 50f,                              // open when 50% of calls fail
        slowCallDurationThreshold = 2.seconds,                   // calls slower than this count as "slow"
        slowCallRateThreshold = 80f,                             // open when 80% of calls are slow
        waitDurationInOpenState = 30.seconds,                    // wait 30s before trying again
        slidingWindowSize = 100,                                 // track the last 100 calls
        slidingWindowType = SlidingWindowType.COUNT_BASED,       // count-based window (default)
        minimumNumberOfCalls = 10,                               // need at least 10 calls before evaluating
        permittedNumberOfCallsInHalfOpenState = 5,               // allow 5 trial calls in half-open
        maxWaitDurationInHalfOpenState = Duration.ZERO,          // no max wait (0 = infinite)
    ),
)
// breaker => CircuitBreaker (state = CLOSED, ready to protect calls)
```

The factory is **idempotent by name** — calling `create("payment-gateway", sameConfig)` again returns the same instance.

#### 2. Execute a protected call

```kotlin
val result = breaker.executeSuspend {
    paymentGateway.charge(amount)
}
// result => CircuitBreakerResult.Success(PaymentResponse(transactionId = "txn_abc123", status = APPROVED))
// If the circuit is open: CircuitBreakerResult.Rejected(CallNotPermitted(...))
// If the call throws: CircuitBreakerResult.Failure(cause = ...)
```

#### 3. Handle results

`executeSuspend` never throws (except `CancellationException`). Pattern-match on the sealed result:

```kotlin
when (val result = breaker.executeSuspend { paymentGateway.charge(amount) }) {
    is CircuitBreakerResult.Success -> result.value
    is CircuitBreakerResult.Rejected -> {
        // Circuit is open — the call was never made
        // result.error.message => "CircuitBreaker 'payment-gateway' does not permit calls"
        log.warn { "Circuit open: ${result.error.message}, using cached response" }
        cachedResponse
    }
    is CircuitBreakerResult.Failure -> {
        // The call was made but threw an exception
        // result.cause => the original exception
        log.error(result.cause) { "Payment call failed" }
        fallbackResponse
    }
}
```

#### 4. Exception handling rules

- `CancellationException` — always propagated (structured concurrency), the permit is released without recording
- `VirtualMachineError` (`OutOfMemoryError`, `StackOverflowError`) — always propagated, permit released without recording
- Other `Error` subclasses — wrapped in `CircuitBreakerResult.Failure` and returned
- All other `Throwable` — wrapped in `CircuitBreakerResult.Failure` and returned
- Circuit open — returned as `CircuitBreakerResult.Rejected` (no network call made)

#### 5. Check metrics

```kotlin
val metrics = breaker.metrics

metrics.state              // => CircuitBreakerState.CLOSED
metrics.failureRate        // => 12.5 (percent) — or -1f if not enough calls yet
metrics.slowCallRate       // => 3.0 (percent)
metrics.numberOfBufferedCalls      // => 47
metrics.numberOfFailedCalls        // => 6
metrics.numberOfSuccessfulCalls    // => 41
metrics.numberOfNotPermittedCalls  // => 0 (only non-zero when circuit was OPEN)
```

#### 6. Manual state control

For operational emergencies (kill-switch, forced recovery):

```kotlin
breaker.trip()   // force circuit OPEN — all calls rejected immediately
breaker.reset()  // force circuit CLOSED — resume normal operation
```

#### 7. Low-level manual recording

For cases where `executeSuspend` doesn't fit (e.g. fire-and-forget, streaming):

```kotlin
if (breaker.tryAcquirePermission()) {
    val start = System.nanoTime()
    try {
        doWork()
        breaker.recordSuccess((System.nanoTime() - start).nanoseconds)
    } catch (e: Exception) {
        breaker.recordFailure((System.nanoTime() - start).nanoseconds, e)
        throw e
    }
} else {
    // circuit is open
}
```

**Warning:** Do not call `tryAcquirePermission()` before `executeSuspend` — it acquires its own permission internally. Acquiring twice leaks a permit.

### Configuration reference

| Parameter | Type | Default | Constraints |
|-----------|------|---------|-------------|
| `failureRateThreshold` | `Float` | — | 1..100 |
| `slowCallDurationThreshold` | `Duration` | — | must be positive |
| `slowCallRateThreshold` | `Float` | — | 1..100 |
| `waitDurationInOpenState` | `Duration` | — | must be positive |
| `slidingWindowSize` | `Int` | — | >= 10 |
| `slidingWindowType` | `SlidingWindowType` | `COUNT_BASED` | `COUNT_BASED` or `TIME_BASED` |
| `minimumNumberOfCalls` | `Int` | — | 1..slidingWindowSize |
| `permittedNumberOfCallsInHalfOpenState` | `Int` | — | > 0 |
| `maxWaitDurationInHalfOpenState` | `Duration` | — | >= 0 (0 = infinite) |
| `recordExceptions` | `Set<Class<out Throwable>>` | `emptySet()` | must not overlap with `ignoreExceptions` |
| `ignoreExceptions` | `Set<Class<out Throwable>>` | `emptySet()` | must not overlap with `recordExceptions` |
| `writableStackTraceEnabled` | `Boolean` | `true` | — |

All parameters without defaults are **required**. Invalid values throw `IllegalArgumentException` at construction time — you can't create a misconfigured circuit breaker.

#### Exception classification

By default, all exceptions are recorded as failures. To customize:

```kotlin
CircuitBreakerConfig(
    // ... other params ...
    recordExceptions = setOf(IOException::class.java, TimeoutException::class.java),
    ignoreExceptions = setOf(BusinessValidationException::class.java),
)
// Only IOException and TimeoutException count as failures.
// BusinessValidationException is ignored (not recorded at all).
// recordExceptions and ignoreExceptions must not overlap — throws IllegalArgumentException if they do.
```

### Using with Spring Boot

The module ships with auto-configuration. Enable it in your `application.yml` and provide a state registry bean.

#### Configuration

Define global defaults and per-instance overrides in YAML. Instance values override global defaults, and global defaults override hardcoded defaults.

```yaml
frisboo:
  corebanking:
    resilience:
      enabled: true
      circuit-breaker:
        enabled: true
        max-breakers: 10000                         # optional, default 10000

        # Global defaults — apply to all instances unless overridden
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 100
        wait-duration-in-open-state: 60s
        sliding-window-size: 100
        sliding-window-type: COUNT_BASED
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 10
        max-wait-duration-in-half-open-state: 0s
        writable-stack-trace-enabled: true

        # Per-instance overrides — only set what differs from global
        instances:
          payment-gateway:
            failure-rate-threshold: 5
            wait-duration-in-open-state: 120s
          email-service:
            failure-rate-threshold: 80
            sliding-window-size: 50
```

With this config, `payment-gateway` inherits all global defaults but overrides `failure-rate-threshold` to 5 and `wait-duration-in-open-state` to 120s.

#### Config-driven creation

When using Spring auto-configuration, you can create circuit breakers by name — the config comes from YAML:

```kotlin
@Service
class PaymentService(
    private val circuitBreakerFactory: CircuitBreakerFactory,
) {
    private lateinit var breaker: CircuitBreaker

    suspend fun init() {
        breaker = circuitBreakerFactory.create("payment-gateway")
        // Config resolved from YAML: failureRateThreshold=5, waitDurationInOpenState=120s, etc.
    }

    suspend fun charge(amount: Amount): PaymentResponse =
        breaker.executeSuspend { gateway.charge(amount) }
}
```

You can still pass explicit config to override YAML entirely:

```kotlin
breaker = circuitBreakerFactory.create(
    name = "payment-gateway",
    config = CircuitBreakerConfig(
        failureRateThreshold = 50f,
        slowCallDurationThreshold = 2.seconds,
        slowCallRateThreshold = 80f,
        waitDurationInOpenState = 30.seconds,
        slidingWindowSize = 100,
        minimumNumberOfCalls = 10,
        permittedNumberOfCallsInHalfOpenState = 5,
        maxWaitDurationInHalfOpenState = Duration.ZERO,
    ),
)
```

#### Provide a state registry

Define a `circuitBreakerStateRegistry` bean — the auto-configuration uses it to persist circuit breaker state across restarts:

```kotlin
@Configuration
class ResilienceConfig {

    @Bean
    @Qualifier("circuitBreakerStateRegistry")
    fun circuitBreakerStateRegistry(): Registry<String, String> {
        TODO("Return your Registry<String, String> implementation (e.g. Redis-backed)")
    }
}
```

Then inject the factory wherever you need it:

```kotlin
@Service
class PaymentService(
    private val circuitBreakerFactory: CircuitBreakerFactory,
) {
    private lateinit var breaker: CircuitBreaker

    suspend fun init() {
        breaker = circuitBreakerFactory.create("payment-gateway")
    }

    suspend fun charge(amount: Amount): PaymentResponse =
        breaker.executeSuspend { gateway.charge(amount) }
}
```

If you need to override the default factory, define your own `CircuitBreakerFactory` bean — the auto-configured one backs off.

#### Test configuration

For tests, use an in-memory registry so no external storage is needed:

```yaml
# src/test/resources/application-test.yml
frisboo:
  corebanking:
    resilience:
      enabled: true
      circuit-breaker:
        enabled: true
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 100
        wait-duration-in-open-state: 5s
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        max-wait-duration-in-half-open-state: 0s
```

```kotlin
@TestConfiguration
class TestResilienceConfig {

    @Bean
    @Qualifier("circuitBreakerStateRegistry")
    fun circuitBreakerStateRegistry(): Registry<String, String> =
        InMemoryRegistryImpl(scope = RegistryScope(name = "cb-state", team = "test"))
}
```

### Config drift protection

If two call sites create a breaker with the same name but different configs, the factory **does not crash**. It replaces the existing breaker with the new config and logs a warning:

```
WARN — CircuitBreaker 'payment-gateway' already exists with a different configuration —
       replacing with the new config. This indicates a config drift that should be fixed.
```

This keeps the API running while surfacing the misconfiguration for operators to fix.

### Config resolution order

When calling `create("payment-gateway")` without explicit config, values are resolved in this order (first non-null wins):

1. **Instance YAML** — `circuit-breaker.instances.payment-gateway.*`
2. **Global YAML** — `circuit-breaker.*` (top-level defaults)
3. **Hardcoded defaults** — `failureRateThreshold=50`, `slowCallDurationThreshold=2s`, `slowCallRateThreshold=100`, `waitDurationInOpenState=60s`, `slidingWindowSize=100`, `minimumNumberOfCalls=10`, `permittedNumberOfCallsInHalfOpenState=10`, `maxWaitDurationInHalfOpenState=0s`, `writableStackTraceEnabled=true`

When calling `create("payment-gateway", config)` with explicit config, YAML is ignored entirely.

### State persistence

Circuit breaker state (CLOSED, OPEN, HALF_OPEN, etc.) is persisted to the `stateStateManager` on every state transition. On restart, the factory restores the last known state so a circuit that was OPEN before a deploy stays OPEN until it naturally recovers.

If state persistence fails (e.g. Redis is down), the failure is logged and the breaker continues with its in-memory state. Persistence is best-effort — it never blocks or crash the breaker.

### Operational notes

- **Eviction**: The factory caches up to `maxBreakers` (default 10,000) instances with a 30-minute access-based TTL. Evicted breakers are recreated on next `create()` call with their persisted state restored.
- **Thread safety**: All operations are thread-safe. The underlying Resilience4j breaker handles concurrency internally.
- **Coroutine-friendly**: `executeSuspend` is a proper suspend function — it never blocks threads.
- **No Micrometer bridge yet**: The circuit breaker exposes metrics via `CircuitBreakerMetrics` but does not currently publish to Micrometer. The `frisboo-observability` module provides a `MetricsRecorder` abstraction — a Micrometer bridge will be added when the observability module gains a Micrometer binding.

---

## Rate Limiter

### What does it do?

A rate limiter controls **how many calls** a service can receive within a time window. When the limit is reached, excess calls are rejected immediately instead of overwhelming the downstream service.

### Getting started

#### 1. Create a rate limiter

```kotlin
val factory: RateLimiterFactory = RateLimiterFactoryImpl(
    stateStateManager = stateStateManager,
    coroutineScope = coroutineScope,
)

val limiter = factory.create(
    name = "payment-gateway",
    config = RateLimiterConfig(
        limitForPeriod = 100,                                // 100 calls per period
        limitRefreshPeriod = Duration.ofSeconds(1),          // period resets every second
        timeoutDuration = Duration.ofMillis(500),            // wait up to 500ms for a permit
    ),
)
```

The factory is **idempotent by name** — calling `create("payment-gateway", sameConfig)` again returns the same instance.

#### 2. Execute a rate-limited call

```kotlin
val result = limiter.executeSuspend {
    paymentGateway.charge(amount)
}
// result => RateLimiterResult.Success(PaymentResponse(...))
// If the limit is exceeded: RateLimiterResult.Rejected(LimitExceeded(requestedPermits = 1))
// If the call throws: RateLimiterResult.Failure(cause = ...)
```

#### 3. Handle results

`executeSuspend` never throws. Pattern-match on the sealed result:

```kotlin
when (val result = limiter.executeSuspend { paymentGateway.charge(amount) }) {
    is RateLimiterResult.Success -> result.value
    is RateLimiterResult.Rejected -> {
        log.warn { "Rate limited: requested ${result.error.requestedPermits} permits" }
        fallbackResponse
    }
    is RateLimiterResult.Failure -> {
        log.error(result.cause) { "Payment call failed" }
        errorResponse
    }
}
```

#### 4. Acquire permits directly

For cases where `executeSuspend` doesn't fit (e.g. batch processing, pre-flight checks):

```kotlin
when (val acquired = limiter.acquire(permits = 5)) {
    is Either.Right -> processBatch()
    is Either.Left -> {
        when (acquired.value) {
            is RateLimiterError.LimitExceeded -> log.warn { "Limit exceeded" }
            is RateLimiterError.Unavailable -> log.error { "Rate limiter unavailable: ${acquired.value.reason}" }
        }
    }
}
```

#### 5. Check metrics

```kotlin
val metrics = limiter.metrics

metrics.availablePermits   // => 87 — permits remaining in the current period
metrics.waitingThreads     // => 0 — threads blocked waiting for a permit
metrics.successfulCalls    // => 13
metrics.rejectedCalls      // => 2
```

### Configuration reference

| Parameter | Type | Default | Constraints |
|-----------|------|---------|-------------|
| `limitForPeriod` | `Int` | — | > 0 |
| `limitRefreshPeriod` | `java.time.Duration` | — | must be positive |
| `timeoutDuration` | `java.time.Duration` | — | >= 0 (0 = no wait) |

All parameters are **required**. Invalid values throw `IllegalArgumentException` at construction time.

### Config drift protection

Same behavior as circuit breaker — if two call sites create a limiter with the same name but different configs, the factory replaces the existing limiter and logs a warning:

```
WARN — RateLimiter 'payment-gateway' already exists with a different configuration —
       replacing with the new config. This indicates a config drift that should be fixed.
```

### Operational notes

- **Eviction**: The factory caches up to `maxLimiters` (default 10,000) instances with a 30-minute access-based TTL.
- **Thread safety**: All operations are thread-safe. The underlying Resilience4j rate limiter handles concurrency internally.
- **Coroutine-friendly**: `executeSuspend` is a proper suspend function — it never blocks threads.
