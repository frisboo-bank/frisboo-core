package com.frisboo.corebanking.core.context

import kotlin.coroutines.CoroutineContext

public class RequestContextElement(
    public val context: RequestContext,
) : CoroutineContext.Element {
    public companion object Key : CoroutineContext.Key<RequestContextElement>

    override val key: CoroutineContext.Key<*> = Key
}
