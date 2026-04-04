package com.frisboo.corebanking.core.factory.decorators

import com.frisboo.corebanking.core.factory.contracts.AsyncFactory

public class MetricsFactory<C : Any, T : Any>(
    private val delegate: AsyncFactory<C, T>,
    // not ready yet
    //    private val meterRegistry: MeterRegistry,
    //    private val metricPrefix: String,
) : AsyncFactory<C, T> {
    override suspend fun getOrCreate(name: String, config: C): T = delegate.getOrCreate(name, config)

    override suspend fun evict(name: String): Unit = delegate.evict(name)

    override suspend fun estimatedSize(): Long = delegate.estimatedSize()
}
