package com.frisboo.corebanking.persistence.redis

import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.coroutines
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.RedisClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Tag("integration")
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisLuaScriptsTest {
    companion object {
        private var integrationEnabled = System.getenv("RUN_INTEGRATION_TESTS") == "true"
        private lateinit var container: GenericContainer<*>
        private lateinit var client: RedisClient
        private lateinit var connection: StatefulRedisConnection<ByteArray, ByteArray>

        private val COMPARE_AND_SET_SCRIPT = """
            local key = KEYS[1]
            local expected = ARGV[1]
            local newValue = ARGV[2]
            local ttl = ARGV[3]

            local function do_set()
              if ttl ~= "__NULL__" and tonumber(ttl) and tonumber(ttl) > 0 then
                redis.call("SET", key, newValue, "PX", tonumber(ttl))
              else
                redis.call("SET", key, newValue)
              end
            end

            local current = redis.call("GET", key)

            if current == false then
                if expected == "__NULL__" then
                    do_set()
                    return 1
                else
                    return -1
                end
            end

            if current ~= expected then
                return -2
            end

            do_set()
            return 1
        """.trimIndent()

        @BeforeAll
        @JvmStatic
        fun setup() {
            // Skip heavy Testcontainers-based integration tests by default.
            // Set environment variable RUN_INTEGRATION_TESTS=true to enable.
            if (!integrationEnabled) return
            container = GenericContainer(DockerImageName.parse("redis:7.0"))
                .withExposedPorts(6379)
            container.start()
            val address = container.host
            val port = container.getMappedPort(6379)
            client = RedisClient.create("redis://$address:$port")
            connection = client.connect(ByteArrayCodec())
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            if (!integrationEnabled) return
            try {
                connection.close()
                client.shutdown()
            } finally {
                container.stop()
            }
        }
    }

    @Test
        fun `compareAndSet sets when key absent and expected null`() = runBlocking {
            val key = "k1".toByteArray()
            val expected = "__NULL__".toByteArray()
            val value = "v1".toByteArray()

            assumeTrue(integrationEnabled, "Integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable")
            val coroutines = connection.coroutines()

            val result = coroutines.eval<Long>(
                COMPARE_AND_SET_SCRIPT,
                ScriptOutputType.INTEGER,
                arrayOf(key),
                expected,
                value,
                "__NULL__".toByteArray(),
            )

        assertEquals(1L, result)
    }

    @Test
        fun `compareAndSet returns -1 when key absent but expected non-null`() = runBlocking {
            val key = "k2".toByteArray()
            val expected = "something".toByteArray()
            val value = "v2".toByteArray()

            assumeTrue(integrationEnabled, "Integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable")
            val coroutines = connection.coroutines()

            val result = coroutines.eval<Long>(
                COMPARE_AND_SET_SCRIPT,
                ScriptOutputType.INTEGER,
                arrayOf(key),
                expected,
                value,
                "__NULL__".toByteArray(),
            )

        assertEquals(-1L, result)
    }

    @Test
        fun `compareAndSet returns -2 on mismatch`() = runBlocking {
            val key = "k3".toByteArray()
            assumeTrue(integrationEnabled, "Integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable")
            val coroutines = connection.coroutines()

            // set initial value
            coroutines.set(key, "initial".toByteArray())

            val expected = "not-initial".toByteArray()
            val value = "v3".toByteArray()

            val result = coroutines.eval<Long>(
                COMPARE_AND_SET_SCRIPT,
                ScriptOutputType.INTEGER,
                arrayOf(key),
                expected,
                value,
                "__NULL__".toByteArray(),
            )

        assertEquals(-2L, result)
    }

    @Test
        fun `compareAndSet updates when expected matches`() = runBlocking {
            val key = "k4".toByteArray()

            assumeTrue(integrationEnabled, "Integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable")
            val coroutines = connection.coroutines()

            coroutines.set(key, "old".toByteArray())

            val expected = "old".toByteArray()
            val value = "new".toByteArray()

            val result = coroutines.eval<Long>(
                COMPARE_AND_SET_SCRIPT,
                ScriptOutputType.INTEGER,
                arrayOf(key),
                expected,
                value,
                "__NULL__".toByteArray(),
            )

        assertEquals(1L, result)
        val read: ByteArray? = coroutines.get(key)
        assertEquals("new", read?.toString(Charsets.UTF_8))
    }
}
