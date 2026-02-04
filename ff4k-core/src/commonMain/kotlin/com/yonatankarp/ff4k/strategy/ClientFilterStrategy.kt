package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature only for requests from specific client hostnames.
 *
 * Requires [ContextKeys.CLIENT_HOSTNAME] to be set in the [FlippingExecutionContext].
 * Throws [IllegalStateException] if no client hostname is present.
 *
 * An empty [grantedClients] set means that no clients are granted access and this
 * strategy will always evaluate to `false`. This matches the behavior of other
 * filter-style strategies (e.g. [AllowListStrategy]) where an empty list denies all.
 *
 * @property grantedClients The set of client hostnames for which the feature is enabled.
 *                          If empty, the feature is disabled for all clients.
 * @throws IllegalStateException if [ContextKeys.CLIENT_HOSTNAME] is not present in the context
 */
@Serializable
@SerialName("clientFilter")
data class ClientFilterStrategy(
    val grantedClients: Set<String> = emptySet(),
) : FlippingStrategy {

    constructor(vararg clients: String) : this(clients.toSet())

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val client = context.getOrThrow<String>(ContextKeys.CLIENT_HOSTNAME)
        return client in grantedClients
    }
}
