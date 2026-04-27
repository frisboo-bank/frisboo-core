package com.frisboo.corebanking.core.context

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
public class RequestContextWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestContext = RequestContextExtractor.extract(exchange)

        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put(RequestContextElement.Key, RequestContextElement(requestContext)) }
    }
}
