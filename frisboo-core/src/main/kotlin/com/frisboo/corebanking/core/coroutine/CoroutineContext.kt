package com.frisboo.corebanking.core.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

public fun <T : Any> T.withCoroutineContext(): CoroutineContext =
    Job() + CoroutineName(this::class.java.name) + Dispatchers.IO

public fun <T : Any> T.withPublisherCoroutineScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(this::class.java.name))
