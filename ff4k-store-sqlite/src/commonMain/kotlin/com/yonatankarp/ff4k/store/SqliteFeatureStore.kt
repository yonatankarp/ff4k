package com.yonatankarp.ff4k.store

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import kotlinx.serialization.modules.SerializersModule

/**
 * A [FeatureStore][com.yonatankarp.ff4k.core.FeatureStore] implementation backed by SQLite.
 *
 * Stores feature flags in a SQLite database using SQLDelight for type-safe queries.
 * Complex fields like permissions, flipping strategies, and custom properties are
 * serialized as JSON. All mutating operations use database transactions to ensure
 * atomicity and prevent race conditions.
 *
 * Example usage:
 * ```kotlin
 * // JVM with in-memory database
 * val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
 * SqliteDatabase.Schema.create(driver).await()
 * val store = SqliteFeatureStore(driver)
 *
 * // Android with file-based database
 * val driver = AndroidSqliteDriver(SqliteDatabase.Schema, context, "features.db")
 * val store = SqliteFeatureStore(driver)
 *
 * // iOS with file-based database
 * val driver = NativeSqliteDriver(SqliteDatabase.Schema, "features.db")
 * val store = SqliteFeatureStore(driver)
 * ```
 *
 * For custom [FlippingStrategy][com.yonatankarp.ff4k.core.FlippingStrategy] or
 * [Property][com.yonatankarp.ff4k.property.Property] implementations, provide a
 * custom serializers module:
 * ```kotlin
 * val customModule = SerializersModule {
 *     polymorphic(FlippingStrategy::class) {
 *         subclass(MyCustomStrategy::class, MyCustomStrategy.serializer())
 *     }
 * }
 * val store = SqliteFeatureStore(driver, customModule)
 * ```
 *
 * @param driver The SQLDelight [SqlDriver] connected to the SQLite database.
 *   The database schema must be created before use.
 * @param customSerializersModule Optional serializers for custom [com.yonatankarp.ff4k.core.FlippingStrategy] or
 *   [com.yonatankarp.ff4k.property.Property] implementations. Merged with built-in FF4K serializers.
 * @see com.yonatankarp.ff4k.core.FeatureStore
 */
class SqliteFeatureStore(
    driver: SqlDriver,
    customSerializersModule: SerializersModule = SerializersModule {},
) : AbstractFeatureStore() {

    private val mapper = SqliteFeatureMapper(customSerializersModule)
    private val database = SqliteDatabase(driver)
    private val queries get() = database.featureQueries

    companion object {
        private const val MAX_RETRIES = 10
        private const val FEATURE_ID_EMPTY_ERROR = "featureId cannot be empty"
    }

    override suspend fun contains(featureId: String): Boolean = queries.exists(featureId).awaitAsOne()

    override suspend fun plusAssign(feature: Feature) {
        require(feature.uid.isNotBlank()) { FEATURE_ID_EMPTY_ERROR }
        try {
            database.transaction {
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
        } catch (e: Exception) {
            if (e.isSqliteUniqueConstraintViolation()) {
                throw FeatureAlreadyExistsException(feature.uid, e)
            }
            throw e
        }
    }

    override suspend fun minusAssign(featureId: String) {
        require(featureId.isNotBlank()) { FEATURE_ID_EMPTY_ERROR }
        database.transaction {
            queries.deleteByUid(featureId)
            val rowsAffected = queries.changes().awaitAsOne()
            if (rowsAffected == 0L) {
                throw FeatureNotFoundException(featureId)
            }
        }
    }

    override suspend fun update(feature: Feature) {
        update(feature.uid) { feature }
    }

    override suspend fun get(featureId: String): Feature? = queries.selectByUid(featureId)
        .awaitAsOneOrNull()
        ?.let { mapper.toDomain(it) }

    override suspend fun getAll(): Map<String, Feature> = queries.selectAll()
        .awaitAsList()
        .associate { it.uid to mapper.toDomain(it) }

    override suspend fun clear() {
        queries.deleteAll()
    }

    override suspend fun isEmpty(): Boolean = queries.count().awaitAsOne() == 0L

    override suspend fun count(): Int = queries.count().awaitAsOne().toInt()

    override suspend fun update(
        featureId: String,
        transform: (Feature) -> Feature,
    ) {
        repeat(MAX_RETRIES) {
            val row = queries.selectByUid(featureId).awaitAsOneOrNull()
                ?: throw FeatureNotFoundException(featureId)

            val feature = mapper.toDomain(row)
            val transformed = transform(feature)

            check(transformed.uid == featureId) {
                "Cannot change feature uid during update. Expected: $featureId, got: ${transformed.uid}"
            }

            val rowsAffected = writeFeature(transformed, expectedVersion = row.version)
            if (rowsAffected > 0) return
        }
        error("Failed to update feature '$featureId' after $MAX_RETRIES retries due to concurrent modifications")
    }

    private suspend fun writeFeature(feature: Feature, expectedVersion: Long): Long {
        var rowsAffected = 0L
        database.transaction {
            queries.update(
                uid = feature.uid,
                enabled = mapper.encodeEnabled(feature.isEnabled),
                group_name = feature.group,
                description = feature.description,
                permissions = mapper.encodePermissions(feature.permissions),
                flipping_strategy = mapper.encodeStrategy(feature.flippingStrategy),
                custom_properties = mapper.encodeProperties(feature.customProperties),
                expectedVersion = expectedVersion,
            )
            val updatedRow = queries.selectByUid(feature.uid).awaitAsOneOrNull()
            rowsAffected =
                if (updatedRow != null && updatedRow.version == expectedVersion + 1) 1L else 0L
        }
        return rowsAffected
    }

    override suspend fun createOrUpdate(feature: Feature) {
        require(feature.uid.isNotBlank()) { FEATURE_ID_EMPTY_ERROR }
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
