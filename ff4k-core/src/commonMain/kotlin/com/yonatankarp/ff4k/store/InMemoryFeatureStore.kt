package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of [AbstractFeatureStore] that stores features in memory.
 *
 * This implementation provides thread-safe operations using a [Mutex] to protect concurrent access.
 * All data is stored in memory and will be lost when the application stops.
 *
 * **Thread Safety**: All operations are protected by a non-reentrant mutex to ensure thread safety.
 * Do not call suspend methods from within locked blocks to avoid deadlocks.
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

    private val features: MutableMap<String, Feature> = initialFeatures.toMutableMap()
    private val mutex: Mutex = Mutex()

    override suspend fun contains(featureId: String): Boolean = mutex.withLock {
        featureId in features
    }

    override suspend fun plusAssign(feature: Feature) = mutex.withLock {
        requireFeatureNotExist(feature.uid)
        features[feature.uid] = feature
    }

    override suspend fun minusAssign(featureId: String): Unit = mutex.withLock {
        requireFeatureExist(featureId)
        features.remove(featureId)
    }

    override suspend fun update(feature: Feature): Unit = mutex.withLock {
        requireFeatureExist(feature.uid)
        features[feature.uid] = feature
    }

    override suspend fun get(featureId: String): Feature? = mutex.withLock {
        features[featureId]
    }

    override suspend fun getAll(): Map<String, Feature> = mutex.withLock {
        features.toMap()
    }

    override suspend fun clear(): Unit = mutex.withLock {
        features.clear()
    }

    override suspend fun isEmpty(): Boolean = mutex.withLock {
        features.isEmpty()
    }

    override suspend fun count(): Int = mutex.withLock { features.size }

    override suspend fun updateFeature(
        featureId: String,
        transform: (Feature) -> Feature,
    ): Unit = mutex.withLock {
        val feature = features[featureId]
            ?: throw FeatureNotFoundException("Feature with id $featureId not found.")
        val transformed = transform(feature)
        check(transformed.uid == featureId) { "Cannot change feature uid during update. Expected: $featureId, got: ${transformed.uid}" }
        features[featureId] = transformed
    }

    override suspend fun createOrUpdate(feature: Feature): Unit = mutex.withLock {
        features[feature.uid] = feature
    }

    override suspend fun toggle(featureId: String): Unit = mutex.withLock {
        val feature = features[featureId]
            ?: throw FeatureNotFoundException("Feature with id $featureId not found.")
        features[featureId] = feature.toggle()
    }

    override suspend fun getOrThrow(featureId: String): Feature = mutex.withLock {
        features[featureId] ?: throw FeatureNotFoundException("Feature with id $featureId not found.")
    }

    /**
     * Validates that a feature exists in the store.
     * This method assumes the caller already holds the mutex lock.
     */
    private fun requireFeatureExist(featureId: String) {
        require(featureId.isNotBlank()) { "featureId cannot be empty" }
        if (featureId !in features) {
            throw FeatureNotFoundException(featureId)
        }
    }

    /**
     * Validates that a feature does not exist in the store.
     * This method assumes the caller already holds the mutex lock.
     */
    private fun requireFeatureNotExist(featureId: String) {
        require(featureId.isNotBlank()) { "featureId cannot be empty" }
        if (featureId in features) {
            throw FeatureAlreadyExistsException(featureId)
        }
    }
}
