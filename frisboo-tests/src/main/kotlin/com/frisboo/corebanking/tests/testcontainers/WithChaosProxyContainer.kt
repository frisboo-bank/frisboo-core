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
): Unit =
    withContext(Dispatchers.IO) {
        require(upstreamAlias.isNotBlank()) { "Upstream alias must not be blank" }
        require(upstreamPort > 0) { "Upstream port must be a positive integer" }

        val proxyName = "chaos-proxy-${UUID.randomUUID()}"

        container.waitingFor(Wait.forListeningPort())

        Network.newNetwork().use { network ->
            val upstreamContainer =
                container.apply {
                    withNetwork(network)
                    withNetworkAliases(upstreamAlias)
                }

            val toxiproxyContainer =
                ToxiproxyContainer(TOXY_PROXY_IMAGE).apply {
                    withNetwork(network)
                }

            try {
                upstreamContainer.start()
                toxiproxyContainer.start()

                val toxiproxyClient = ToxiproxyClient(toxiproxyContainer.host, toxiproxyContainer.controlPort)
                val proxy =
                    toxiproxyClient.createProxy(
                        proxyName,
                        "0.0.0.0:$proxyListenPort",
                        "$upstreamAlias:$upstreamPort",
                    )

                val proxyHost = toxiproxyContainer.host
                val proxyPort = toxiproxyContainer.getMappedPort(proxyListenPort)

                val context =
                    ChaosProxyContext(
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
