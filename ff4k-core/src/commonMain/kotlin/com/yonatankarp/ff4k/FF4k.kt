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
     * Returns true when the feature exists, is enabled, the caller holds one of its permissions (if any)
     * and its strategy (if any) accepts the [context]. Unknown features are reported as disabled.
     *
     * The caller's roles are read from `context[`[ROLES]`]` as a collection of strings. A feature with
     * permissions is inactive when no roles are given.
     */
    suspend fun check(
        id: String,
        context: Map<String, Any> = emptyMap(),
    ): Boolean {
        val feature = features.get(id) ?: return false
        return feature.enabled &&
            feature.isPermitted(context) &&
            (feature.strategy?.evaluate(feature, context) ?: true)
    }

    /** Returns the value of the property [name] when it exists and is of type [T], otherwise null. */
    suspend inline fun <reified T : Any> property(name: String): T? = properties.get(name)?.value as? T

    private fun Feature.isPermitted(context: Map<String, Any>): Boolean {
        if (permissions.isEmpty()) return true
        val roles = context[ROLES] as? Iterable<*> ?: return false
        return roles.any { it is String && it in permissions }
    }

    companion object {
        /** Context key holding the caller's roles, matched against [Feature.permissions]. */
        const val ROLES = "roles"
    }
}
