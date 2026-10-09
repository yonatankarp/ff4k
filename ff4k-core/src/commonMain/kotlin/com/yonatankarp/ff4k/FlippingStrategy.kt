package com.yonatankarp.ff4k

/** Decides whether an enabled [feature] is active for the given evaluation [context]. */
fun interface FlippingStrategy {
    suspend fun evaluate(
        feature: Feature,
        context: Map<String, Any>,
    ): Boolean
}
