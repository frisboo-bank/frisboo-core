# frisboo-statemanager

Type-safe, scoped key-value registries with per-entry TTL, encryption, audit logging, and resilience decorators

## Which one do I need?

| Use case | Interface / Class | Backing store |
|----------|------------------|---------------|
| Unit tests, prototyping, or single-instance deployments | `InMemoryStateManagerImpl` | `HashMap` + `Mutex` |
| Production single-instance with size-bounded cache and automatic eviction | `CaffeineStateManagerImpl` | Caffeine `Cache` (lock-free) |
| Multi-instance / distributed deployments | `StateManagerRedisImpl` | Redis via Lettuce (Lua-atomic) |
| Add PCI-DSS audit trail to any statemanager | `AuditingStateManagerImpl` (decorator) | Wraps any `StateManager` |
| Add circuit-breaking / fallback to any statemanager | `ResilientStateManagerImpl` (decorator) | Wraps primary + fallback `StateManager` |
| Track hit/miss/put/eviction/failure counters | `InstrumentedStateManagerImpl` (decorator) | Wraps any `StateManager`, exposes `StateManagerMetrics` |
| Encrypt values at rest (AES-GCM via `frisboo-crypto`) | `EncryptingSerializerImpl` | Wraps any `StateManagerSerializer` |

Not sure? Start with `CaffeineStateManagerImpl` for local caches, `StateManagerRedisImpl` for shared state. Decorators compose — stack them as needed.

## Core interfaces

```
StateManagerReader<K, V>    get, contains, size
StateManagerWriter<K, V>    put, getOrPut, evict
StateManagerAdmin<K, V>     keys, keysPage, setTTL
        │
        └── StateManager<K, V>  (combines all three)
                │
        ┌───────┴───────┐
LocalStateManager<K, V>    DistributedStateManager<K, V>
  + cleanupExpired()      + isHealthy()
```

Depend on the **narrowest** sub-interface you need (`StateManagerReader`, `StateManagerWriter`, or `StateManagerAdmin`).

## Getting started

### 1. In-memory statemanager (tests and prototyping)

```kotlin
val scope = StateManagerScope(name = "session-tokens", team = "payments")
// scope.prefix => "payments:session-tokens"

val statemanager: LocalStateManager<String, String> = InMemoryStateManagerImpl(scope)

// Store with a TTL
val putResult = statemanager.put("txn-001", "approved", ttl = 30.seconds)
// putResult => PutResult.Created

// Read back
val value = statemanager.get("txn-001")
// value => "approved"

val missing = statemanager.get("txn-999")
// missing => null
```

### 2. Caffeine-backed statemanager (production local cache)

```kotlin
val statemanager: LocalStateManager<String, String> = CaffeineStateManagerImpl(
    scope = StateManagerScope(name = "rate-limits", team = "gateway"),
    maximumSize = 50_000,     // entries; evicts LRU beyond this
    defaultTtl = 5.minutes,   // applied when put() is called without explicit ttl
)

val putResult = statemanager.put("client-abc", "120")
// putResult => PutResult.Created

// Atomic get-or-put — factory runs exactly once for a given key
val result = statemanager.getOrPut("client-abc", ttl = 10.minutes) { "100" }
// result => GetOrPutResult.Found(value = "120")  (existing entry returned, factory not invoked)

val result2 = statemanager.getOrPut("client-xyz", ttl = 10.minutes) { "100" }
// result2 => GetOrPutResult.Created(value = "100")  (factory invoked, new entry stored)

// Best-effort expired entry cleanup
val removed = statemanager.cleanupExpired()
// removed => 0  (no expired entries yet)
```

### 3. Redis-backed statemanager (distributed)

```kotlin
// You supply: Lettuce connection + key/value serializers
val statemanager: DistributedStateManager<String, String> = StateManagerRedisImpl(
    scope = StateManagerScope(name = "idempotency", team = "payments"),
    connection = redisConnection,           // StatefulRedisConnection<ByteArray, ByteArray>
    keySerializer = StringStateManagerSerializer,
    valueSerializer = StringStateManagerSerializer,
    operationTimeout = 3.seconds,           // per-operation timeout; default is 5s
)

// All writes are Lua-atomic on the Redis server
val putResult = statemanager.put("req-abc-123", "processed", ttl = 24.hours)
// putResult => PutResult.Created

// Health probe for circuit-breaker / readiness checks
val healthy = statemanager.isHealthy()
// healthy => true  (Redis responded PONG within timeout)
```

**TTL requirement**: Redis requires `ttl >= 1ms`. Sub-millisecond values return `PutResult.Failed`.

### 4. Cursor-based pagination

```kotlin
// Prefer keysPage() over keys() in production — keys() is O(N) full scan
var cursor: String? = null
do {
    val page = statemanager.keysPage(cursor = cursor, limit = 100)
    // page.items => ["txn-001", "txn-002", ...]  (up to 100 keys)
    // page.nextCursor => "100" or null when done
    process(page.items)
    cursor = page.nextCursor
} while (cursor != null)
```

### 5. Encrypt values at rest

```kotlin
val plaintextSerializer: StateManagerSerializer<String> = StringStateManagerSerializer

val encryptingSerializer: StateManagerSerializer<String> = EncryptingSerializerImpl(
    delegate = plaintextSerializer,
    cryptoService = tinkCryptoService,   // from frisboo-crypto
    scope = StateManagerScope(name = "secrets", team = "vault"),
)
// AAD is bound to scope prefix ("vault:secrets") — ciphertext cannot be reused across scopes

val redis: DistributedStateManager<String, String> = StateManagerRedisImpl(
    scope = StateManagerScope(name = "secrets", team = "vault"),
    connection = redisConnection,
    keySerializer = StringStateManagerSerializer,
    valueSerializer = encryptingSerializer,   // transparently encrypts/decrypts
)

redis.put("api-key-prod", "sk_live_abc123", ttl = 1.hours)
// Stored in Redis as AES-256-GCM ciphertext — not readable without the keyset
```

## Handle errors

All operations return sealed result types — no exceptions for expected failures:

```kotlin
when (val result = statemanager.put("key", "value", ttl = Duration.ZERO)) {
    is PutResult.Created -> println("new entry")
    is PutResult.Updated -> println("overwritten")
    is PutResult.Failed  -> when (result.error) {
        is StateManagerError.InvalidTtl       -> println(result.error.message)
        // => "TTL must be positive, got: 0s"
        is StateManagerError.ConnectionFailed  -> println(result.error.message)
        is StateManagerError.OperationTimeout  -> println("timed out after ${result.error.timeoutMillis}ms")
        is StateManagerError.ScopeViolation    -> println(result.error.message)
    }
}
```

`getOrPut` returns `GetOrPutResult<V>` with the same pattern:

```kotlin
when (val result = statemanager.getOrPut("key") { computeExpensiveValue() }) {
    is GetOrPutResult.Created -> println("computed: ${result.value}")
    is GetOrPutResult.Found   -> println("cached: ${result.value}")
    is GetOrPutResult.Failed  -> println("error: ${result.error}")
}
```

`StateManagerScope` validates names at construction time — invalid names throw `IllegalArgumentException`:

```kotlin
StateManagerScope(name = "valid-name", team = "payments")
// => StateManagerScope(name = "valid-name", team = "payments")

StateManagerScope(name = "INVALID!", team = "payments")
// => IllegalArgumentException: "StateManager scope name must match ^[a-z]([a-z0-9\-]{0,61}[a-z0-9])?$, got: 'INVALID!'"
```

## Decorators

Decorators compose via delegation. Stack them in any order:

### Audit logging (PCI-DSS 10.x, SOC 2 CC6/CC7)

```kotlin
val audited: StateManager<String, String> = AuditingStateManagerImpl(
    delegate = statemanager,
    scope = StateManagerScope(name = "card-tokens", team = "payments"),
    auditEmitter = { event -> auditLog.write(event) },
    auditHmacKey = hmacKeyBytes,   // HMAC-SHA256 key for resource ID hashing
)

// Every put/evict/getOrPut/setTTL emits a StateManagerAuditEvent:
// StateManagerAuditEvent(
//     action = StateManagerAuditAction.PUT,           // typed, not a raw string
//     scope = "payments:card-tokens",
//     resourceId = "hmac-sha256:a1b2c3d4...",     // HMAC of actual key — no PII in logs
//     success = true,
//     details = {"ttl": "30s"},
// )
```

### Resilience (circuit-breaking + fallback)

```kotlin
val resilient: StateManager<String, String> = ResilientStateManagerImpl(
    primary = redisStateManager,
    fallback = caffeineStateManager,
    resilientExecutor = circuitBreakerExecutor,   // implements ResilienceExecutor
)

// If Redis is down, operations transparently fall back to Caffeine.
// CancellationException is always rethrown — structured concurrency is preserved.
```

### Composing decorators

```kotlin
// Redis → Encrypted values → Audit trail → Circuit-breaker fallback to Caffeine
val statemanager: StateManager<String, String> = ResilientStateManagerImpl(
    primary = AuditingStateManagerImpl(
        delegate = redisStateManager,   // uses EncryptingSerializerImpl for values
        scope = scope,
        auditEmitter = emitter,
        auditHmacKey = hmacKey,
    ),
    fallback = caffeineStateManager,
    resilientExecutor = executor,
)
```

### Metrics (Micrometer gauge binding)

```kotlin
val instrumented = InstrumentedStateManagerImpl(delegate = statemanager)
// instrumented implements both StateManager<K, V> and StateManagerMetrics

// Bind to Micrometer gauges for Prometheus scraping
meterStateManager.gauge("statemanager.hits", instrumented) { it.hitCount.toDouble() }
// gauge => statemanager.hits{} (current hit count, monotonically increasing)

meterStateManager.gauge("statemanager.misses", instrumented) { it.missCount.toDouble() }
// gauge => statemanager.misses{} (current miss count)

meterStateManager.gauge("statemanager.puts", instrumented) { it.putCount.toDouble() }
// gauge => statemanager.puts{} (total entries written)

meterStateManager.gauge("statemanager.evictions", instrumented) { it.evictionCount.toDouble() }
// gauge => statemanager.evictions{} (total entries evicted)

meterStateManager.gauge("statemanager.failures", instrumented) { it.failureCount.toDouble() }
// gauge => statemanager.failures{} (total failed operations)

// Read counters directly for diagnostics
val ratio = if (instrumented.hitCount + instrumented.missCount > 0) {
    instrumented.hitCount.toDouble() / (instrumented.hitCount + instrumented.missCount)
} else 0.0
// ratio => 0.85 (85% cache hit rate)
```

Counters are lock-free (`AtomicLong`) — reading them has zero impact on statemanager throughput.

## Using with Spring Boot

Define beans for each layer and let Spring compose them:

```kotlin
@Configuration
class StateManagerConfig {

    @Bean
    fun paymentStateManagerScope(): StateManagerScope =
        StateManagerScope(name = "idempotency", team = "payments")

    @Bean
    fun paymentStateManager(
        scope: StateManagerScope,
        redisConnection: StatefulRedisConnection<ByteArray, ByteArray>,
        cryptoService: CryptoService,
        auditEmitter: StateManagerAuditEmitter,
        @Value("\${statemanager.audit-hmac-key}") hmacKey: ByteArray,
        resilientExecutor: ResilienceExecutor,
    ): StateManager<String, String> {
        val encryptingSerializer = EncryptingSerializerImpl(
            delegate = StringStateManagerSerializer,
            cryptoService = cryptoService,
            scope = scope,
        )

        val redis: DistributedStateManager<String, String> = StateManagerRedisImpl(
            scope = scope,
            connection = redisConnection,
            keySerializer = StringStateManagerSerializer,
            valueSerializer = encryptingSerializer,
        )

        val fallback: LocalStateManager<String, String> = CaffeineStateManagerImpl(
            scope = scope,
            maximumSize = 10_000,
        )

        val audited = AuditingStateManagerImpl(
            delegate = redis,
            scope = scope,
            auditEmitter = auditEmitter,
            auditHmacKey = hmacKey,
        )

        return ResilientStateManagerImpl(
            primary = audited,
            fallback = fallback,
            resilientExecutor = resilientExecutor,
        )
    }
}
```

Then inject wherever you need it:

```kotlin
@Service
class PaymentService(
    private val statemanager: StateManager<String, String>,
) {
    suspend fun processPayment(requestId: String, payload: String): String {
        val result = statemanager.getOrPut(requestId, ttl = 24.hours) { payload }
        return when (result) {
            is GetOrPutResult.Found   -> "duplicate — returning cached result"
            is GetOrPutResult.Created -> "processed"
            is GetOrPutResult.Failed  -> "rejected: ${result.error}"
        }
        // First call  => GetOrPutResult.Created(value = "...payload...")
        // Retry call  => GetOrPutResult.Found(value = "...payload...")
    }
}
```

## Operational notes

- **`keys()` is O(N)** — use `keysPage()` with cursor-based pagination in production. `keys()` caps at 100,000 results and SCAN iterations at 10,000 to prevent runaway loops.
- **`size()` is O(N)** on Redis (SCAN-based) — avoid in hot paths. Local implementations are O(1).
- **TTL precision**: local registries accept any positive `Duration`; Redis requires `>= 1ms` (sub-ms values return `Failed`).
- **Thread safety**: all implementations are safe for concurrent coroutine use. `InMemoryStateManagerImpl` uses `Mutex`, `CaffeineStateManagerImpl` is lock-free via Caffeine, `StateManagerRedisImpl` is lock-free via Lua atomicity.
- **Scope isolation**: `StateManagerScope` enforces DNS-like naming (`^[a-z]([a-z0-9\-]{0,61}[a-z0-9])?$`) to prevent key-injection attacks across tenants in Redis.
- **`getOrPut` factory contract**: the factory may be invoked speculatively under concurrent races (particularly on Redis). Ensure your factory is safe to call more than once for the same key.
- **Metrics**: `InstrumentedStateManagerImpl` counters are monotonically increasing and never reset. For rate-based dashboards, use Prometheus `rate()` or `increase()` over the raw gauge values.
- **Cleanup**: `LocalStateManager.cleanupExpired()` is best-effort — Caffeine's count may be slightly inflated by concurrent size-evictions.
