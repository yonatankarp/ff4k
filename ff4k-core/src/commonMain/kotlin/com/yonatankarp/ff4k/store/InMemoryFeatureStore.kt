package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException
import com.yonatankarp.ff4k.FeatureStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryFeatureStore(
    initial: List<Feature> = emptyList(),
) : FeatureStore {
    private val features = initial.associateBy { it.id }.toMutableMap()
    private val mutex = Mutex()

    override suspend fun get(id: String): Feature? = mutex.withLock { features[id] }

    override suspend fun getAll(): List<Feature> = mutex.withLock { features.values.toList() }

    override suspend fun put(feature: Feature) {
        mutex.withLock { features[feature.id] = feature }
    }

    override suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature = mutex.withLock {
        val updated = transform(features[id] ?: throw FeatureNotFoundException(id))
        require(updated.id == id) { "Cannot change feature id during update: expected '$id', got '${updated.id}'" }
        features[id] = updated
        updated
    }

    override suspend fun delete(id: String) {
        mutex.withLock { features.remove(id) }
    }
}
