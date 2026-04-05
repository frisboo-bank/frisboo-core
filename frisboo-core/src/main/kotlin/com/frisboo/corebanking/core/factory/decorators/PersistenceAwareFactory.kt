//package com.frisboo.corebanking.core.factory.decorators
//
//import com.frisboo.corebanking.core.factory.contracts.AsyncFactory
//import com.frisboo.corebanking.registry.contracts.Registry
//
//public class PersistenceAwareFactory<C : Any, T : Any>(
//    private val delegate: AsyncFactory<C, T>,
//    private val stateRegistry: Registry<String, String>,
//    private val stateKeyPrefix: String,
//    private val stateApplier: suspend (T, String) -> Unit,
//    private val stateExtractor: suspend (T) -> String? = { null },
//) : AsyncFactory<C, T> {
//
//    override suspend fun getOrCreate(name: String, config: C): T {
//        val instance = delegate.getOrCreate(name, config)
//        val stateKey = "$stateKeyPrefix$name"
//
//        stateRegistry.get(stateKey)?.let { savedState ->
//            stateApplier(instance, savedState)
//        }
//
//        stateExtractor(instance)?.let { currentState ->
//            stateRegistry.put(stateKey, currentState)
//        }
//
//        return instance
//    }
//
//    override suspend fun evict(name: String) {
//        delegate.evict(name)
//        stateRegistry.evict("$stateKeyPrefix$name")
//    }
//
//    override suspend fun estimatedSize(): Long = delegate.estimatedSize()
//}
