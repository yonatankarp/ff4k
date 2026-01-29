package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.config.FF4kConfiguration
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.utils.withReentrantLock
import kotlinx.coroutines.sync.Mutex

/**
 * In-memory implementation of [AbstractFeatureStore] that stores features in memory.
 *
 * This implementation provides thread-safe operations using a [Mutex] to protect concurrent access.
 * All data is stored in memory and will be lost when the application stops.
 *
 * **Thread Safety**: All operations are protected by a reentrant mutex mechanism to ensure thread safety.
 * Internal suspend methods can be safely called from within locked blocks.
 *
 * **Use Cases**:
 * - Testing and development
 * - Simple applications where persistence is not required
 * - Prototyping feature flag systems
 *
 * @param initialFeatures Optional map of features to initialize the store with
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
class InMemoryFeatureStore(
    initialFeatures: Map<String, Feature> = emptyMap(),
) : AbstractFeatureStore() {

    constructor(config: FF4kConfiguration) : this(config.features)

    private val features: MutableMap<String, Feature> = initialFeatures.toMutableMap()
    private val mutex: Mutex = Mutex()

    override suspend fun contains(featureId: String): Boolean = mutex.withReentrantLock {
        featureId in features
    }

    override suspend fun plusAssign(feature: Feature) = mutex.withReentrantLock {
        requireFeatureNotExist(feature.uid)
        features[feature.uid] = feature
    }

    override suspend fun minusAssign(featureId: String): Unit = mutex.withReentrantLock {
        requireFeatureExist(featureId)
        features.remove(featureId)
    }

    override suspend fun update(feature: Feature): Unit = mutex.withReentrantLock {
        requireFeatureExist(feature.uid)
        features[feature.uid] = feature
    }

    override suspend fun get(featureId: String): Feature? = mutex.withReentrantLock {
        features[featureId]
    }

    override suspend fun getAll(): Map<String, Feature> = mutex.withReentrantLock {
        features.toMap()
    }

    override suspend fun clear(): Unit = mutex.withReentrantLock {
        features.clear()
    }

    override suspend fun isEmpty(): Boolean = mutex.withReentrantLock {
        features.isEmpty()
    }

    override suspend fun count(): Int = mutex.withReentrantLock { features.size }

    override suspend fun updateFeature(
        featureId: String,
        transform: (Feature) -> Feature,
    ): Unit = mutex.withReentrantLock {
        val feature = getOrThrow(featureId)
        val transformed = transform(feature)
        check(transformed.uid == featureId) { "Cannot change feature uid during update. Expected: $featureId, got: ${transformed.uid}" }
        update(transformed)
    }

    override suspend fun createOrUpdate(feature: Feature): Unit = mutex.withReentrantLock {
        super.createOrUpdate(feature)
    }
}
