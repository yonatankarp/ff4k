package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureNotFoundException

/**
 * The optimistic-locking loop behind [com.yonatankarp.ff4k.FeatureStore.update], for stores that can write
 * conditionally. [read] returns a snapshot of the stored feature, or null when there is none; [decode] turns it
 * into a [Feature]; [write] stores the updated feature only if the stored data still matches the snapshot and
 * returns whether it did. The loop retries until a write lands.
 *
 * @throws FeatureNotFoundException when [read] finds no feature.
 * @throws IllegalArgumentException when [transform] changes the feature's id.
 */
suspend fun <S> optimisticUpdate(
    id: String,
    transform: (Feature) -> Feature,
    read: suspend () -> S?,
    decode: (S) -> Feature,
    write: suspend (snapshot: S, updated: Feature) -> Boolean,
): Feature {
    while (true) {
        val snapshot = read() ?: throw FeatureNotFoundException(id)
        val updated = transform(decode(snapshot))
        require(updated.id == id) { "Cannot change feature id during update: expected '$id', got '${updated.id}'" }
        if (write(snapshot, updated)) return updated
    }
}
