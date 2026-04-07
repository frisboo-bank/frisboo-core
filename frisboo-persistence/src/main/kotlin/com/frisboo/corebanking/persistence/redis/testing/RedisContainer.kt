package com.frisboo.corebanking.persistence.redis.testing

import com.frisboo.corebanking.persistence.redis.constants.REDIS_IMAGE
import com.redis.testcontainers.RedisContainer
import org.testcontainers.utility.MountableFile

public fun redisContainer(
    dockerImageName: String = REDIS_IMAGE,
): RedisContainer = RedisContainer(dockerImageName).apply {
//    withCopyFileToContainer(
//        MountableFile.forClasspathResource("configs/redis.conf"),
//        "/usr/local/etc/redis/redis.conf",
//    )
//    withCommand("redis-server", "/usr/local/etc/redis/redis.conf")
}

public suspend fun withRedisContainer(
    container: RedisContainer,
    block: suspend (host: String, port: Int) -> Unit,
) {
    container.start()
    try {
        block(container.host, container.firstMappedPort)
    } finally {
        container.stop()
    }
}
