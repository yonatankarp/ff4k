package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Serves reads from a snapshot of the store refreshed every [ttl]; writes go through and expire the snapshot.
 * When a refresh fails, reads keep the last snapshot and the refresh is retried after [ttl]. Before the first
 * successful load, reads rethrow the failure until that retry.
 */
fun FeatureStore.cached(
    ttl: Duration = 30.seconds,
    timeSource: TimeSource = TimeSource.Monotonic,
): FeatureStore = CachedFeatureStore(this, ttl, timeSource)

/**
 * Serves reads from a snapshot of the store refreshed every [ttl]; writes go through and expire the snapshot.
 * When a refresh fails, reads keep the last snapshot and the refresh is retried after [ttl]. Before the first
 * successful load, reads rethrow the failure until that retry.
 */
fun PropertyStore.cached(
    ttl: Duration = 30.seconds,
    timeSource: TimeSource = TimeSource.Monotonic,
): PropertyStore = CachedPropertyStore(this, ttl, timeSource)

class CachedFeatureStore(
    private val delegate: FeatureStore,
    ttl: Duration,
    timeSource: TimeSource = TimeSource.Monotonic,
) : FeatureStore {
    private val snapshot = Snapshot(ttl, timeSource, Feature::id) { delegate.getAll() }

    override suspend fun get(id: String): Feature? = snapshot.get()[id]

    override suspend fun getAll(): List<Feature> = snapshot.get().values.toList()

    override suspend fun put(feature: Feature) {
        delegate.put(feature)
        snapshot.invalidate()
    }

    override suspend fun update(
        id: String,
        transform: (Feature) -> Feature,
    ): Feature = delegate.update(id, transform).also { snapshot.invalidate() }

    override suspend fun delete(id: String) {
        delegate.delete(id)
        snapshot.invalidate()
    }
}

class CachedPropertyStore(
    private val delegate: PropertyStore,
    ttl: Duration,
    timeSource: TimeSource = TimeSource.Monotonic,
) : PropertyStore {
    private val snapshot = Snapshot(ttl, timeSource, Property<Any>::name) { delegate.getAll() }

    override suspend fun get(name: String): Property<Any>? = snapshot.get()[name]

    override suspend fun getAll(): List<Property<Any>> = snapshot.get().values.toList()

    override suspend fun put(property: Property<Any>) {
        delegate.put(property)
        snapshot.invalidate()
    }

    override suspend fun delete(name: String) {
        delegate.delete(name)
        snapshot.invalidate()
    }
}

private class Snapshot<K, V>(
    private val ttl: Duration,
    private val timeSource: TimeSource,
    private val key: (V) -> K,
    private val load: suspend () -> List<V>,
) {
    private val mutex = Mutex()
    private var values: Map<K, V>? = null
    private var failure: Exception? = null
    private var expiresAt: TimeMark? = null

    // shortcut: callers queue behind a slow load, refresh in the background if a hung backend becomes a problem
    suspend fun get(): Map<K, V> = mutex.withLock {
        if (expiresAt?.hasNotPassedNow() != true) refresh()
        values ?: throw checkNotNull(failure)
    }

    // shortcut: a failed refresh is not reported anywhere, surface it once the audit hook (#25) exists
    private suspend fun refresh() {
        try {
            values = load().associateBy(key)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure = e
        }
        expiresAt = timeSource.markNow() + ttl
    }

    /** Keeps the values: they are the fallback if the next load fails. */
    suspend fun invalidate() = mutex.withLock { expiresAt = null }
}
