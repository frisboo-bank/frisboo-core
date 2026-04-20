# frisboo-crypto

Encrypt and decrypt sensitive data in Frisboo modules

## Which one do I need?

| Use case | Interface | Example |
|----------|-----------|---------|
| Encrypt data that **your own service** will decrypt later | `CryptoService` | Storing encrypted PII, config secrets, registry entries |
| Encrypt data that **only another party** should decrypt | `HybridCryptoService` | Merchant payment payloads, inter-service key exchange, anything where the sender shouldn't be able to read it back |

Not sure? Start with `CryptoService`. Use `HybridCryptoService` only when the encryptor and decryptor are different entities.

## Getting started

### 1. Encrypt / decrypt your own data

```kotlin
val crypto: CryptoService = TinkCryptoService(keysetHandle)

// Encrypt
val ciphertext = crypto.encrypt(
    plaintext = "account-number-1234".toByteArray(Charsets.UTF_8),
    associatedData = "tenant:acme:scope:payments".toByteArray(Charsets.UTF_8),
)
// ciphertext => [1, 84, 22, 171, 63, ...] (opaque Tink ciphertext, ~45 bytes for this input)

// Decrypt (same associated data, same key)
val plaintext = crypto.decrypt(ciphertext, associatedData = "tenant:acme:scope:payments".toByteArray(Charsets.UTF_8))
// plaintext => "account-number-1234".toByteArray(Charsets.UTF_8)
// String(plaintext) => "account-number-1234"
```

**Always pass `associatedData`.** It ties the ciphertext to a context (tenant, scope, record ID) so it can't be copied to a different context and still decrypt. Empty plaintext and empty associated data are rejected at runtime.

### 2. Encrypt for someone else to decrypt

```kotlin
val hybrid: HybridCryptoService = TinkHybridCryptoService(privateKeysetHandle)

// Encrypt (anyone with the public key can do this)
val ciphertext = hybrid.encrypt(
    plaintext = "card-data-json".toByteArray(Charsets.UTF_8),
    contextInfo = "merchant:shop-123:txn:abc".toByteArray(Charsets.UTF_8),
)
// ciphertext => [0, 4, 112, 55, 201, ...] (opaque HPKE ciphertext, larger than AEAD due to encapsulated key)

// Decrypt (only the private key holder)
val plaintext = hybrid.decrypt(ciphertext, contextInfo = "merchant:shop-123:txn:abc".toByteArray(Charsets.UTF_8))
// plaintext => "card-data-json".toByteArray(Charsets.UTF_8)
// String(plaintext) => "card-data-json"
```

Same rule: **always pass `contextInfo`** to bind the ciphertext to a specific transaction or context. Empty plaintext and empty context info are rejected at runtime.

### 3. Store ciphertext as a String

Ciphertext is a raw `ByteArray` — not valid UTF-8 — so Base64-encode it before storing in a text column, JSON field, or API response:

```kotlin
import java.util.Base64

val ciphertext = crypto.encrypt(
    plaintext = "account-number-1234".toByteArray(Charsets.UTF_8),
    associatedData = "tenant:acme:scope:payments".toByteArray(Charsets.UTF_8),
)

// ByteArray → String (safe for DB text columns, JSON, HTTP headers)
val encoded: String = Base64.getEncoder().encodeToString(ciphertext)
// encoded => "AVQW0j8xK7nQ2f1mHp..." (URL-safe printable characters)

// String → ByteArray (to decrypt later)
val decoded: ByteArray = Base64.getDecoder().decode(encoded)
val plaintext = crypto.decrypt(decoded, associatedData = "tenant:acme:scope:payments".toByteArray(Charsets.UTF_8))
// String(plaintext) => "account-number-1234"
```

### 4. Handle errors

All crypto failures are subclasses of `CryptoException` (sealed class), so you can catch them precisely:

```kotlin
try {
    crypto.decrypt(ciphertext, associatedData)
} catch (e: CryptoException.DecryptionFailed) {
    // ciphertext was tampered with, wrong key, or mismatched associated data
    // e.message => "Decryption failed"
    // e.cause  => GeneralSecurityException (from Tink)
} catch (e: CryptoException.InitializationFailed) {
    // keyset is invalid or misconfigured (wrong primitive type, corrupt key material)
    // e.message => "Failed to obtain AEAD primitive from keyset"
}
```

For hybrid: `CryptoException.HybridEncryptionFailed` and `CryptoException.HybridDecryptionFailed`.

Invalid inputs throw `IllegalArgumentException` before any crypto operation runs:

```kotlin
crypto.encrypt(byteArrayOf(), associatedData)
// => IllegalArgumentException: "plaintext must not be empty"

crypto.encrypt(plaintext, byteArrayOf())
// => IllegalArgumentException: "associatedData must not be empty — supply a context binding (e.g. tenantId:objectType:objectId)"
```

You can also catch the parent `CryptoException` if you just want a single handler for all crypto errors.

## Using with Spring Boot

The module ships with auto-configuration. Tink initialization and service bean creation are handled for you — enable it in your `application.yml` and provide your keyset(s).

### Configuration

```yaml
frisboo:
  corebanking:
    crypto:
      enabled: true
      aead:
        enabled: true    # set to true if you need symmetric encryption
      hybrid:
        enabled: true    # set to true if you need asymmetric encryption
```

### AEAD (symmetric encryption)

Enable `aead` in your properties and define an `aeadKeysetHandle` bean — a `CryptoService` bean will be created automatically:

```kotlin
@Configuration
class CryptoKeyConfig {

    @Bean
    @Qualifier("aeadKeysetHandle")
    fun aeadKeysetHandle(): KeysetHandle {
        TODO("Load your AEAD keyset from KMS or secure storage")
    }
}
```

Then inject wherever you need it:

```kotlin
@Service
class PaymentService(
    private val crypto: CryptoService,
) {
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    fun storeCardFingerprint(cardNumber: String, tenantId: String): String {
        val encrypted = crypto.encrypt(
            plaintext = cardNumber.toByteArray(Charsets.UTF_8),
            associatedData = "tenant:$tenantId".toByteArray(Charsets.UTF_8),
        )
        // Base64-encode for storage in a text column
        return encoder.encodeToString(encrypted)
        // => "AVQW0j8xK7nQ2f1mHp..."
    }

    fun readCardFingerprint(stored: String, tenantId: String): String {
        val decrypted = crypto.decrypt(
            ciphertext = decoder.decode(stored),
            associatedData = "tenant:$tenantId".toByteArray(Charsets.UTF_8),
        )
        return String(decrypted)
        // => "4111-1111-1111-1234"
    }
}
```

### Hybrid (asymmetric encryption)

Enable `hybrid` in your properties and define a `hybridKeysetHandle` bean:

```kotlin
@Bean
@Qualifier("hybridKeysetHandle")
fun hybridKeysetHandle(): KeysetHandle {
    TODO("Load your hybrid private keyset from KMS or secure storage")
}
```

You can enable one or both. The auto-configuration only creates beans for what you've enabled and provided keysets for.

If you need to override the default service implementation, define your own `CryptoService` or `HybridCryptoService` bean — the auto-configured one backs off.

### Test configuration

For tests, enable crypto in your test properties and use `KeysetHandleFactory` for ephemeral in-memory keys:

```yaml
# src/test/resources/application-test.yml
frisboo:
  corebanking:
    crypto:
      enabled: true
      aead:
        enabled: true
      hybrid:
        enabled: true
```

```kotlin
@TestConfiguration
class TestCryptoConfig {

    @Bean
    @Qualifier("aeadKeysetHandle")
    fun aeadKeysetHandle(): KeysetHandle = KeysetHandleFactory.generateAes256Gcm()

    @Bean
    @Qualifier("hybridKeysetHandle")
    fun hybridKeysetHandle(): KeysetHandle = KeysetHandleFactory.generateHybridHpke()
}
```

## Key management

This module handles encryption — **not** where your keys live. In production:

- Store keysets in a KMS (GCP KMS, AWS KMS, HashiCorp Vault)
- Never persist plaintext key material to disk, config files, or environment variables
- Rotate keys regularly; Tink keysets support multiple keys with one primary, so rotation doesn't require re-encrypting existing data

`KeysetHandleFactory` is for tests and local dev only. Don't use it in production.
