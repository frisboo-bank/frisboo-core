package com.frisboo.corebanking.tests.testcontainers

import com.frisboo.corebanking.tests.constants.TOXY_PROXY_IMAGE
import eu.rekawek.toxiproxy.Proxy
import eu.rekawek.toxiproxy.ToxiproxyClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.toxiproxy.ToxiproxyContainer
import java.util.UUID

public data class ChaosProxyContext(
    public val proxyHost: String,
    public val proxyPort: Int,
    public val proxy: Proxy,
    public val upstreamContainer: GenericContainer<*>,
)

/**
 * Utility function to run a block of code with a Toxiproxy proxy set up for a given container.
 *
 * @param container The target container to proxy.
 * @param upstreamAlias The network alias for the target container within the proxy network
 * @param upstreamPort The port on which the target container is listening for connections.
 * @param block The function to execute with the proxy host, port, and proxy instance
 */
public suspend fun <C : GenericContainer<*>> withChaosProxyContainer(
    container: C,
    upstreamAlias: String,
    upstreamPort: Int,
    proxyListenPort: Int = 8666,
    block: suspend ChaosProxyContext.() -> Unit,
): Unit = withContext(Dispatchers.IO) {
    require(upstreamAlias.isNotBlank()) { "Upstream alias must not be blank" }
    require(upstreamPort > 0) { "Upstream port must be a positive integer" }

    val proxyName = "chaos-proxy-${UUID.randomUUID()}"

    container.waitingFor(Wait.forListeningPort())

    Network.newNetwork().use { network ->
        val upstreamContainer = container.apply {
            withNetwork(network)
            withNetworkAliases(upstreamAlias)
        }

        val toxiproxyContainer = ToxiproxyContainer(TOXY_PROXY_IMAGE).apply {
            withNetwork(network)
        }

        try {
            upstreamContainer.start()
            toxiproxyContainer.start()

            val toxiproxyClient = ToxiproxyClient(toxiproxyContainer.host, toxiproxyContainer.controlPort)
            val proxy = toxiproxyClient.createProxy(
                proxyName,
                "0.0.0.0:${proxyListenPort}",
                "$upstreamAlias:$upstreamPort",
            )

            val proxyHost = toxiproxyContainer.host
            val proxyPort = toxiproxyContainer.getMappedPort(proxyListenPort)

            val context = ChaosProxyContext(
                proxyHost = proxyHost,
                proxyPort = proxyPort,
                proxy = proxy,
                upstreamContainer = upstreamContainer,
            )
            block(context)
        } finally {
            toxiproxyContainer.stop()
            upstreamContainer.stop()
        }
    }
}

