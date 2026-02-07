package com.yonatankarp.ff4k.store.sqldelight

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.store.AbstractFeatureStore
import kotlinx.serialization.modules.SerializersModule

/**
 * SQLDelight-backed implementation of [AbstractFeatureStore] that persists features to a database.
 *
 * This store uses SQLDelight for type-safe database access and supports any database backend
 * that SQLDelight supports (SQLite, PostgreSQL, MySQL, etc.) via the provided [SqlDriver].
 *
 * ## Setup
 *
 * 1. Create the database schema before using the store:
 *    ```kotlin
 *    FF4kDatabase.Schema.create(driver).await()
 *    ```
 *
 * 2. Create the store with your driver:
 *    ```kotlin
 *    val store = SqlDelightFeatureStore(driver)
 *    ```
 *
 * ## Custom Serialization Support
 *
 * To use custom [FlippingStrategy] or [Property] implementations, provide a [SerializersModule]:
 *
 * ```kotlin
 * val customModule = SerializersModule {
 *     polymorphic(FlippingStrategy::class) {
 *         subclass(MyCustomStrategy::class, MyCustomStrategy.serializer())
 *     }
 * }
 * val store = SqlDelightFeatureStore(driver, customModule)
 * ```
 *
 * ## Thread Safety
 *
 * This implementation relies on database transactions for atomicity. Concurrent access
 * is safe when using a properly configured database driver.
 *
 * @param driver The SQLDelight [SqlDriver] for database access.
 * @param customSerializersModule Optional [SerializersModule] for custom [FlippingStrategy]
 *   or [Property] serialization. Custom types must be registered here to be persisted correctly.
 * @see FF4kDatabase.Schema For schema creation and migration.
 */
class SqlDelightFeatureStore(
    driver: SqlDriver,
    customSerializersModule: SerializersModule = SerializersModule {},
) : AbstractFeatureStore() {

    private val mapper = FeatureMapper(customSerializersModule)
    private val database = FF4kDatabase(driver)
    private val queries get() = database.featureQueries

    override suspend fun contains(featureId: String): Boolean = queries.exists(featureId).awaitAsOne()

    override suspend fun plusAssign(feature: Feature) {
        requireFeatureNotExist(feature.uid)
        queries.insert(
            uid = feature.uid,
            enabled = mapper.encodeEnabled(feature.isEnabled),
            group_name = feature.group,
            description = feature.description,
            permissions = mapper.encodePermissions(feature.permissions),
            flipping_strategy = mapper.encodeStrategy(feature.flippingStrategy),
            custom_properties = mapper.encodeProperties(feature.customProperties),
        )
    }

    override suspend fun minusAssign(featureId: String) {
        requireFeatureExist(featureId)
        queries.deleteByUid(featureId)
    }

    override suspend fun update(feature: Feature) {
        requireFeatureExist(feature.uid)
        queries.update(
            uid = feature.uid,
            enabled = mapper.encodeEnabled(feature.isEnabled),
            group_name = feature.group,
            description = feature.description,
            permissions = mapper.encodePermissions(feature.permissions),
            flipping_strategy = mapper.encodeStrategy(feature.flippingStrategy),
            custom_properties = mapper.encodeProperties(feature.customProperties),
        )
    }

    override suspend fun get(featureId: String): Feature? = queries.selectByUid(featureId).awaitAsOneOrNull()?.let { mapper.toDomain(it) }

    override suspend fun getAll(): Map<String, Feature> = queries.selectAll().awaitAsList()
        .associate { it.uid to mapper.toDomain(it) }

    override suspend fun clear() {
        queries.deleteAll()
    }

    override suspend fun isEmpty(): Boolean = queries.count().awaitAsOne() == 0L

    override suspend fun count(): Int = queries.count().awaitAsOne().toInt()

    override suspend fun updateFeature(featureId: String, transform: (Feature) -> Feature) {
        database.transaction {
            val feature = getOrThrow(featureId)
            val transformed = transform(feature)
            check(transformed.uid == featureId) {
                "Cannot change feature uid during update. Expected: $featureId, got: ${transformed.uid}"
            }
            update(transformed)
        }
    }

    override suspend fun createOrUpdate(feature: Feature) {
        queries.upsert(
            uid = feature.uid,
            enabled = mapper.encodeEnabled(feature.isEnabled),
            group_name = feature.group,
            description = feature.description,
            permissions = mapper.encodePermissions(feature.permissions),
            flipping_strategy = mapper.encodeStrategy(feature.flippingStrategy),
            custom_properties = mapper.encodeProperties(feature.customProperties),
        )
    }
}
