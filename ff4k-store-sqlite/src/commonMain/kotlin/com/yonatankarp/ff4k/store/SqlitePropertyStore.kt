package com.yonatankarp.ff4k.store

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.yonatankarp.ff4k.exception.PropertyAlreadyExistsException
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.store.sqldelight.sqlite.SqliteDatabase
import kotlinx.serialization.modules.SerializersModule

/**
 * SQLite-backed property store for persisting configuration properties.
 *
 * Use this store when you need durable property storage with SQLite. Properties are
 * serialized as JSON, enabling storage of any property type including custom implementations.
 *
 * ### Optimistic Locking
 * Operations that update existing properties (such as `updateProperty`) use optimistic
 * locking to prevent lost updates when multiple clients modify the same property
 * concurrently. If a concurrent modification is detected, the operation retries
 * automatically (up to 10 times) before failing.
 *
 * Note: `createOrUpdate` performs a plain upsert with last-write-wins semantics and does
 * not use optimistic locking.
 *
 * ### Setup
 * You must create the database schema before using the store:
 * ```kotlin
 * // JVM with in-memory database
 * val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
 * SqliteDatabase.Schema.create(driver).await()
 * val store = SqlitePropertyStore(driver)
 *
 * // Android
 * val driver = AndroidSqliteDriver(SqliteDatabase.Schema, context, "ff4k.db")
 * val store = SqlitePropertyStore(driver)
 *
 * // iOS
 * val driver = NativeSqliteDriver(SqliteDatabase.Schema, "ff4k.db")
 * val store = SqlitePropertyStore(driver)
 * ```
 *
 * ### Custom Property Types
 * To store custom property implementations, provide a serializers module:
 * ```kotlin
 * val customModule = SerializersModule {
 *     polymorphic(Property::class) {
 *         subclass(MyCustomProperty::class, MyCustomProperty.serializer())
 *     }
 * }
 * val store = SqlitePropertyStore(driver, customModule)
 * ```
 *
 * @param driver The SQLDelight driver connected to your SQLite database.
 * @param customSerializersModule Serializers for custom property types you want to store.
 */
class SqlitePropertyStore(
    driver: SqlDriver,
    customSerializersModule: SerializersModule = SerializersModule {},
) : AbstractPropertyStore() {

    private val mapper = SqlitePropertyMapper(customSerializersModule)
    private val database = SqliteDatabase(driver)
    private val queries get() = database.propertyQueries

    companion object {
        private const val MAX_RETRIES = 10
    }

    override suspend fun requirePropertyExist(name: String) {
        require(name.isNotBlank()) { "propertyId cannot be empty" }
        if (!queries.exists(name).awaitAsOne()) {
            throw PropertyNotFoundException(name)
        }
    }

    override suspend fun requirePropertyNotExist(name: String) {
        require(name.isNotBlank()) { "propertyId cannot be empty" }
        if (queries.exists(name).awaitAsOne()) {
            throw PropertyAlreadyExistsException(name)
        }
    }

    override suspend fun isEmpty(): Boolean = queries.count().awaitAsOne() == 0L

    override suspend fun contains(propertyId: String): Boolean = queries.exists(propertyId).awaitAsOne()

    override suspend fun <T> plusAssign(property: Property<T>) {
        database.transaction {
            requirePropertyNotExist(property.name)
            queries.insert(
                name = property.name,
                type = mapper.encodeType(property),
                description = property.description,
                read_only = mapper.encodeReadOnly(property.readOnly),
                data_ = mapper.encodeData(property),
            )
        }
    }

    override suspend fun minusAssign(propertyId: String) {
        database.transaction {
            requirePropertyExist(propertyId)
            queries.deleteByName(propertyId)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(propertyId: String): Property<T>? = queries.selectByName(propertyId)
        .awaitAsOneOrNull()
        ?.let { mapper.toDomain(it) as Property<T> }

    override suspend fun <T> updateProperty(property: Property<T>) {
        updateProperty(property.name) { property }
    }

    override suspend fun <T> updateProperty(
        name: String,
        transform: (Property<T>) -> Property<T>,
    ) {
        repeat(MAX_RETRIES) {
            val row = queries.selectByName(name).awaitAsOneOrNull()
                ?: throw PropertyNotFoundException(name)

            @Suppress("UNCHECKED_CAST")
            val property = mapper.toDomain(row) as Property<T>
            val transformed = transform(property)

            require(transformed.name == name) {
                "Cannot change property name during update. Expected: $name, got: ${transformed.name}"
            }

            val rowsAffected = writeProperty(transformed, expectedVersion = row.version)
            if (rowsAffected > 0) return
        }
        error("Failed to update property '$name' after $MAX_RETRIES retries due to concurrent modifications")
    }

    private suspend fun <T> writeProperty(property: Property<T>, expectedVersion: Long): Long {
        var rowsAffected = 0L
        database.transaction {
            queries.update(
                name = property.name,
                type = mapper.encodeType(property),
                description = property.description,
                read_only = mapper.encodeReadOnly(property.readOnly),
                data = mapper.encodeData(property),
                expectedVersion = expectedVersion,
            )
            val updatedRow = queries.selectByName(property.name).awaitAsOneOrNull()
            rowsAffected =
                if (updatedRow != null && updatedRow.version == expectedVersion + 1) 1L else 0L
        }
        return rowsAffected
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> getOrDefault(
        propertyId: String,
        defaultValue: Property<T>,
    ): Property<T> = get(propertyId) ?: defaultValue

    override suspend fun getAll(): Map<String, Property<*>> = queries.selectAll()
        .awaitAsList()
        .associate { it.name to mapper.toDomain(it) }

    override suspend fun listPropertyIds(): Set<String> = queries.listNames()
        .awaitAsList()
        .toSet()

    override suspend fun clear() {
        queries.deleteAll()
    }

    /**
     * Stores a property, creating it if new or updating it if it exists.
     *
     * Use this when you want to ensure a property exists with specific values,
     * regardless of whether it was previously stored. Unlike [plusAssign], this
     * won't fail if the property already exists.
     *
     * @param property The property to store.
     */
    override suspend fun <T> createOrUpdate(property: Property<T>) {
        queries.upsert(
            name = property.name,
            type = mapper.encodeType(property),
            description = property.description,
            read_only = mapper.encodeReadOnly(property.readOnly),
            data_ = mapper.encodeData(property),
        )
    }
}
