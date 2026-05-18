package com.frisboo.corebanking.core.context

import kotlinx.coroutines.currentCoroutineContext

public suspend fun currentRequestContext(): RequestContext? =
    currentCoroutineContext()[RequestContextElement]?.context
