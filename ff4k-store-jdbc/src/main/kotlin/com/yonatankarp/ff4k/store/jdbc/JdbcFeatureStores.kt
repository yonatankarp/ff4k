package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.store.sql.SqlDialect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.modules.SerializersModule
import javax.sql.DataSource

/**
 * Creates a JDBC-backed [FeatureStore] with an explicit SQL dialect.
 *
 * Use this factory function to create a feature store that persists features
 * in a relational database via JDBC. The dialect parameter determines the
 * database-specific SQL syntax used for all operations.
 *
 * Example:
 * ```kotlin
 * val store = jdbcFeatureStore(HikariDataSource(config), PostgresDialect)
 * store += Feature(uid = "my-feature", isEnabled = true)
 * ```
 *
 * For custom [FlippingStrategy][com.yonatankarp.ff4k.core.FlippingStrategy] or
 * [Property][com.yonatankarp.ff4k.property.Property] implementations:
 * ```kotlin
 * val customModule = SerializersModule {
 *     polymorphic(FlippingStrategy::class) {
 *         subclass(MyCustomStrategy::class, MyCustomStrategy.serializer())
 *     }
 * }
 * val store = jdbcFeatureStore(dataSource, PostgresDialect, customModule)
 * ```
 *
 * @param dataSource The JDBC [DataSource] for database connections.
 * @param dialect The [SqlDialect] for SQL statements.
 * @param customSerializersModule Optional serializers for custom types.
 * @param ioDispatcher The [CoroutineDispatcher] to use for blocking JDBC operations.
 *   Defaults to [Dispatchers.IO].
 */
suspend fun jdbcFeatureStore(
    dataSource: DataSource,
    dialect: SqlDialect,
    customSerializersModule: SerializersModule = SerializersModule {},
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): FeatureStore = JdbcFeatureStore(dataSource, dialect, customSerializersModule, ioDispatcher).also { it.ensureSchema() }
