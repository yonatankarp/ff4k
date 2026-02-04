package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A strategy that enables a feature only on specific server hostnames.
 *
 * Useful for canary deployments or testing features on specific server instances.
 * Requires [ContextKeys.SERVER_HOSTNAME] to be set in the [FlippingExecutionContext].
 * Throws [IllegalStateException] if no server hostname is present.
 *
 * Note: If [targetServers] is empty (the default), no servers are granted access and this
 * strategy will always return `false` for any server hostname.
 *
 * @property targetServers The set of server hostnames on which the feature is enabled.
 * If this set is empty, the feature is disabled for all servers.
 * @throws IllegalStateException if [ContextKeys.SERVER_HOSTNAME] is not present in the context
 */
@Serializable
@SerialName("serverFilter")
data class ServerFilterStrategy(
    val targetServers: Set<String> = emptySet(),
) : FlippingStrategy {

    constructor(vararg servers: String) : this(servers.toSet())

    override suspend fun evaluate(
        featureId: String,
        store: FeatureStore?,
        context: FlippingExecutionContext,
    ): Boolean {
        val server = context.getOrThrow<String>(ContextKeys.SERVER_HOSTNAME)
        return server in targetServers
    }
}
