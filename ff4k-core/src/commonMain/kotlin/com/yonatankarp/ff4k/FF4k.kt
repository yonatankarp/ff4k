package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.store.InMemoryPropertyStore

/**
 * Entry point of the library: evaluates feature flags against a [FeatureStore] and exposes typed properties.
 */
class FF4k(
    val features: FeatureStore = InMemoryFeatureStore(),
    val properties: PropertyStore = InMemoryPropertyStore(),
) {
    constructor(configuration: FF4kConfiguration) : this(
        features = InMemoryFeatureStore(configuration.features),
        properties = InMemoryPropertyStore(configuration.properties),
    )

    /**
     * Returns true when the feature exists, is enabled and its strategy (if any) accepts the [context].
     * Unknown features are reported as disabled.
     */
    suspend fun check(
        id: String,
        context: Map<String, Any> = emptyMap(),
    ): Boolean {
        val feature = features.get(id) ?: return false
        return feature.enabled && (feature.strategy?.evaluate(feature, context) ?: true)
    }

    /** Returns the value of the property [name] when it exists and is of type [T], otherwise null. */
    suspend inline fun <reified T : Any> property(name: String): T? = properties.get(name)?.value as? T
}
