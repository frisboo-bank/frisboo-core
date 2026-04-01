# frisboo-registry

Type-safe, scoped key-value registries with per-entry TTL, encryption, audit logging, and resilience decorators

## Which one do I need?

| Use case | Interface / Class | Backing store |
|----------|------------------|---------------|
| Unit tests, prototyping, or single-instance deployments | `InMemoryRegistryImpl` | `HashMap` + `Mutex` |
| Production single-instance with size-bounded cache and automatic eviction | `CaffeineRegistryImpl` | Caffeine `Cache` (lock-free) |
| Multi-instance / distributed deployments | `RedisRegistryImpl` | Redis via Lettuce (Lua-atomic) |
| Add PCI-DSS audit trail to any registry | `AuditingRegistryImpl` (decorator) | Wraps any `Registry` |
| Add circuit-breaking / fallback to any registry | `ResilientRegistryImpl` (decorator) | Wraps primary + fallback `Registry` |
| Track hit/miss/put/eviction/failure counters | `InstrumentedRegistryImpl` (decorator) | Wraps any `Registry`, exposes `RegistryMetrics` |
| Encrypt values at rest (AES-GCM via `frisboo-crypto`) | `EncryptingSerializerImpl` | Wraps any `RegistrySerializer` |

Not sure? Start with `CaffeineRegistryImpl` for local caches, `RedisRegistryImpl` for shared state. Decorators compose — stack them as needed.

## Core interfaces

```
RegistryReader<K, V>    get, contains, size
RegistryWriter<K, V>    put, getOrPut, evict
RegistryAdmin<K, V>     keys, keysPage, setTTL
        │
        └── Registry<K, V>  (combines all three)
                │
        ┌───────┴───────┐
LocalRegistry<K, V>    DistributedRegistry<K, V>
  + cleanupExpired()      + isHealthy()
```

Depend on the **narrowest** sub-interface you need (`RegistryReader`, `RegistryWriter`, or `RegistryAdmin`).

## Getting started

### 1. In-memory registry (tests and prototyping)

```kotlin
val scope = RegistryScope(name = "session-tokens", team = "payments")
// scope.prefix => "payments:session-tokens"

val registry: LocalRegistry<String, String> = InMemoryRegistryImpl(scope)

// Store with a TTL
val putResult = registry.put("txn-001", "approved", ttl = 30.seconds)
// putResult => PutResult.Created

// Read back
val value = registry.get("txn-001")
// value => "approved"

val missing = registry.get("txn-999")
// missing => null
```

### 2. Caffeine-backed registry (production local cache)

```kotlin
val registry: LocalRegistry<String, String> = CaffeineRegistryImpl(
    scope = RegistryScope(name = "rate-limits", team = "gateway"),
    maximumSize = 50_000,     // entries; evicts LRU beyond this
    defaultTtl = 5.minutes,   // applied when put() is called without explicit ttl
)

val putResult = registry.put("client-abc", "120")
// putResult => PutResult.Created

// Atomic get-or-put — factory runs exactly once for a given key
val result = registry.getOrPut("client-abc", ttl = 10.minutes) { "100" }
// result => GetOrPutResult.Found(value = "120")  (existing entry returned, factory not invoked)

val result2 = registry.getOrPut("client-xyz", ttl = 10.minutes) { "100" }
// result2 => GetOrPutResult.Created(value = "100")  (factory invoked, new entry stored)

// Best-effort expired entry cleanup
val removed = registry.cleanupExpired()
// removed => 0  (no expired entries yet)
```

### 3. Redis-backed registry (distributed)

```kotlin
// You supply: Lettuce connection + key/value serializers
val registry: DistributedRegistry<String, String> = RedisRegistryImpl(
    scope = RegistryScope(name = "idempotency", team = "payments"),
    connection = redisConnection,           // StatefulRedisConnection<ByteArray, ByteArray>
    keySerializer = StringRegistrySerializer,
    valueSerializer = StringRegistrySerializer,
    operationTimeout = 3.seconds,           // per-operation timeout; default is 5s
)

// All writes are Lua-atomic on the Redis server
val putResult = registry.put("req-abc-123", "processed", ttl = 24.hours)
// putResult => PutResult.Created

// Health probe for circuit-breaker / readiness checks
val healthy = registry.isHealthy()
// healthy => true  (Redis responded PONG within timeout)
```

**TTL requirement**: Redis requires `ttl >= 1ms`. Sub-millisecond values return `PutResult.Failed`.

### 4. Cursor-based pagination

```kotlin
// Prefer keysPage() over keys() in production — keys() is O(N) full scan
var cursor: String? = null
do {
    val page = registry.keysPage(cursor = cursor, limit = 100)
    // page.items => ["txn-001", "txn-002", ...]  (up to 100 keys)
    // page.nextCursor => "100" or null when done
    process(page.items)
    cursor = page.nextCursor
} while (cursor != null)
```

### 5. Encrypt values at rest

```kotlin
val plaintextSerializer: RegistrySerializer<String> = StringRegistrySerializer

val encryptingSerializer: RegistrySerializer<String> = EncryptingSerializerImpl(
    delegate = plaintextSerializer,
    cryptoService = tinkCryptoService,   // from frisboo-crypto
    scope = RegistryScope(name = "secrets", team = "vault"),
)
// AAD is bound to scope prefix ("vault:secrets") — ciphertext cannot be reused across scopes

val redis: DistributedRegistry<String, String> = RedisRegistryImpl(
    scope = RegistryScope(name = "secrets", team = "vault"),
    connection = redisConnection,
    keySerializer = StringRegistrySerializer,
    valueSerializer = encryptingSerializer,   // transparently encrypts/decrypts
)

redis.put("api-key-prod", "sk_live_abc123", ttl = 1.hours)
// Stored in Redis as AES-256-GCM ciphertext — not readable without the keyset
```

## Handle errors

All operations return sealed result types — no exceptions for expected failures:

```kotlin
when (val result = registry.put("key", "value", ttl = Duration.ZERO)) {
    is PutResult.Created -> println("new entry")
    is PutResult.Updated -> println("overwritten")
    is PutResult.Failed  -> when (result.error) {
        is RegistryError.InvalidTtl       -> println(result.error.message)
        // => "TTL must be positive, got: 0s"
        is RegistryError.ConnectionFailed  -> println(result.error.message)
        is RegistryError.OperationTimeout  -> println("timed out after ${result.error.timeoutMillis}ms")
        is RegistryError.ScopeViolation    -> println(result.error.message)
    }
}
```

`getOrPut` returns `GetOrPutResult<V>` with the same pattern:

```kotlin
when (val result = registry.getOrPut("key") { computeExpensiveValue() }) {
    is GetOrPutResult.Created -> println("computed: ${result.value}")
    is GetOrPutResult.Found   -> println("cached: ${result.value}")
    is GetOrPutResult.Failed  -> println("error: ${result.error}")
}
```

`RegistryScope` validates names at construction time — invalid names throw `IllegalArgumentException`:

```kotlin
RegistryScope(name = "valid-name", team = "payments")
// => RegistryScope(name = "valid-name", team = "payments")

RegistryScope(name = "INVALID!", team = "payments")
// => IllegalArgumentException: "Registry scope name must match ^[a-z]([a-z0-9\-]{0,61}[a-z0-9])?$, got: 'INVALID!'"
```

## Decorators

Decorators compose via delegation. Stack them in any order:

### Audit logging (PCI-DSS 10.x, SOC 2 CC6/CC7)

```kotlin
val audited: Registry<String, String> = AuditingRegistryImpl(
    delegate = registry,
    scope = RegistryScope(name = "card-tokens", team = "payments"),
    auditEmitter = { event -> auditLog.write(event) },
    auditHmacKey = hmacKeyBytes,   // HMAC-SHA256 key for resource ID hashing
)

// Every put/evict/getOrPut/setTTL emits a RegistryAuditEvent:
// RegistryAuditEvent(
//     action = RegistryAuditAction.PUT,           // typed, not a raw string
//     scope = "payments:card-tokens",
//     resourceId = "hmac-sha256:a1b2c3d4...",     // HMAC of actual key — no PII in logs
//     success = true,
//     details = {"ttl": "30s"},
// )
```

### Resilience (circuit-breaking + fallback)

```kotlin
val resilient: Registry<String, String> = ResilientRegistryImpl(
    primary = redisRegistry,
    fallback = caffeineRegistry,
    resilientExecutor = circuitBreakerExecutor,   // implements ResilienceExecutor
)

// If Redis is down, operations transparently fall back to Caffeine.
// CancellationException is always rethrown — structured concurrency is preserved.
```

### Composing decorators

```kotlin
// Redis → Encrypted values → Audit trail → Circuit-breaker fallback to Caffeine
val registry: Registry<String, String> = ResilientRegistryImpl(
    primary = AuditingRegistryImpl(
        delegate = redisRegistry,   // uses EncryptingSerializerImpl for values
        scope = scope,
        auditEmitter = emitter,
        auditHmacKey = hmacKey,
    ),
    fallback = caffeineRegistry,
    resilientExecutor = executor,
)
```

### Metrics (Micrometer gauge binding)

```kotlin
val instrumented = InstrumentedRegistryImpl(delegate = registry)
// instrumented implements both Registry<K, V> and RegistryMetrics

// Bind to Micrometer gauges for Prometheus scraping
meterRegistry.gauge("registry.hits", instrumented) { it.hitCount.toDouble() }
// gauge => registry.hits{} (current hit count, monotonically increasing)

meterRegistry.gauge("registry.misses", instrumented) { it.missCount.toDouble() }
// gauge => registry.misses{} (current miss count)

meterRegistry.gauge("registry.puts", instrumented) { it.putCount.toDouble() }
// gauge => registry.puts{} (total entries written)

meterRegistry.gauge("registry.evictions", instrumented) { it.evictionCount.toDouble() }
// gauge => registry.evictions{} (total entries evicted)

meterRegistry.gauge("registry.failures", instrumented) { it.failureCount.toDouble() }
// gauge => registry.failures{} (total failed operations)

// Read counters directly for diagnostics
val ratio = if (instrumented.hitCount + instrumented.missCount > 0) {
    instrumented.hitCount.toDouble() / (instrumented.hitCount + instrumented.missCount)
} else 0.0
// ratio => 0.85 (85% cache hit rate)
```

Counters are lock-free (`AtomicLong`) — reading them has zero impact on registry throughput.

## Using with Spring Boot

Define beans for each layer and let Spring compose them:

```kotlin
@Configuration
class RegistryConfig {

    @Bean
    fun paymentRegistryScope(): RegistryScope =
        RegistryScope(name = "idempotency", team = "payments")

    @Bean
    fun paymentRegistry(
        scope: RegistryScope,
        redisConnection: StatefulRedisConnection<ByteArray, ByteArray>,
        cryptoService: CryptoService,
        auditEmitter: RegistryAuditEmitter,
        @Value("\${registry.audit-hmac-key}") hmacKey: ByteArray,
        resilientExecutor: ResilienceExecutor,
    ): Registry<String, String> {
        val encryptingSerializer = EncryptingSerializerImpl(
            delegate = StringRegistrySerializer,
            cryptoService = cryptoService,
            scope = scope,
        )

        val redis: DistributedRegistry<String, String> = RedisRegistryImpl(
            scope = scope,
            connection = redisConnection,
            keySerializer = StringRegistrySerializer,
            valueSerializer = encryptingSerializer,
        )

        val fallback: LocalRegistry<String, String> = CaffeineRegistryImpl(
            scope = scope,
            maximumSize = 10_000,
        )

        val audited = AuditingRegistryImpl(
            delegate = redis,
            scope = scope,
            auditEmitter = auditEmitter,
            auditHmacKey = hmacKey,
        )

        return ResilientRegistryImpl(
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
    private val registry: Registry<String, String>,
) {
    suspend fun processPayment(requestId: String, payload: String): String {
        val result = registry.getOrPut(requestId, ttl = 24.hours) { payload }
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
- **Thread safety**: all implementations are safe for concurrent coroutine use. `InMemoryRegistryImpl` uses `Mutex`, `CaffeineRegistryImpl` is lock-free via Caffeine, `RedisRegistryImpl` is lock-free via Lua atomicity.
- **Scope isolation**: `RegistryScope` enforces DNS-like naming (`^[a-z]([a-z0-9\-]{0,61}[a-z0-9])?$`) to prevent key-injection attacks across tenants in Redis.
- **`getOrPut` factory contract**: the factory may be invoked speculatively under concurrent races (particularly on Redis). Ensure your factory is safe to call more than once for the same key.
- **Metrics**: `InstrumentedRegistryImpl` counters are monotonically increasing and never reset. For rate-based dashboards, use Prometheus `rate()` or `increase()` over the raw gauge values.
- **Cleanup**: `LocalRegistry.cleanupExpired()` is best-effort — Caffeine's count may be slightly inflated by concurrent size-evictions.
