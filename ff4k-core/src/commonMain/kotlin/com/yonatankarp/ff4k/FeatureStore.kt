package com.yonatankarp.ff4k

interface FeatureStore {
    suspend fun get(id: String): Feature?

    suspend fun getAll(): List<Feature>

    /** Inserts or replaces the feature. */
    suspend fun put(feature: Feature)

    /**
     * Atomically replaces the stored feature with the result of [transform].
     * @throws FeatureNotFoundException when no feature with [id] exists.
     */
    suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature

    /** Removes the feature; a no-op when it does not exist. */
    suspend fun delete(id: String)
}

class FeatureNotFoundException(id: String) : NoSuchElementException("Feature not found: $id")

suspend fun FeatureStore.enable(id: String): Feature = update(id) { it.copy(enabled = true) }

suspend fun FeatureStore.disable(id: String): Feature = update(id) { it.copy(enabled = false) }

suspend fun FeatureStore.group(name: String): List<Feature> = getAll().filter { it.group == name }

suspend fun FeatureStore.enableGroup(name: String) = group(name).forEach { enable(it.id) }

suspend fun FeatureStore.disableGroup(name: String) = group(name).forEach { disable(it.id) }
