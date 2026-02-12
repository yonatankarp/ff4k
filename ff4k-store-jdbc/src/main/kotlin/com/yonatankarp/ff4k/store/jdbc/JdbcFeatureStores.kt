package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.store.sql.PostgresDialect
import com.yonatankarp.ff4k.store.sql.SqlDialect
import kotlinx.serialization.modules.SerializersModule
import javax.sql.DataSource

/**
 * Creates a JDBC-backed [FeatureStore] that auto-detects the database type.
 *
 * The database type is detected using JDBC metadata from the [DataSource].
 * The feature table is created automatically if it doesn't exist.
 *
 * Example:
 * ```kotlin
 * val store = jdbcFeatureStore(HikariDataSource(config))
 * store += Feature(uid = "my-feature", isEnabled = true)
 * ```
 *
 * For custom [FlippingStrategy][com.yonatankarp.ff4k.core.FlippingStrategy] or
 * [Property][com.yonatankarp.ff4k.property.Property] implementations:
 * ```kotlin
 * val store = jdbcFeatureStore(dataSource) {
 *     polymorphic(FlippingStrategy::class) {
 *         subclass(MyCustomStrategy::class, MyCustomStrategy.serializer())
 *     }
 * }
 * ```
 *
 * @param dataSource The JDBC [DataSource] for database connections.
 * @param customSerializersModule Optional serializers for custom types.
 * @throws UnsupportedDatabaseException If the database type is not supported.
 */
fun jdbcFeatureStore(
    dataSource: DataSource,
    customSerializersModule: SerializersModule = SerializersModule {},
): FeatureStore = jdbcFeatureStore(dataSource, detectDialect(dataSource), customSerializersModule)

/**
 * Creates a JDBC-backed [FeatureStore] with an explicit SQL dialect.
 *
 * Use this when automatic detection doesn't work (e.g., database proxies).
 *
 * @param dataSource The JDBC [DataSource] for database connections.
 * @param dialect The [SqlDialect] for SQL statements.
 * @param customSerializersModule Optional serializers for custom types.
 */
fun jdbcFeatureStore(
    dataSource: DataSource,
    dialect: SqlDialect,
    customSerializersModule: SerializersModule = SerializersModule {},
): FeatureStore = JdbcFeatureStore(dataSource, dialect, customSerializersModule).also { it.ensureSchema() }

private fun detectDialect(dataSource: DataSource): SqlDialect =
    dataSource.connection.use { conn ->
        val productName = conn.metaData.databaseProductName.lowercase()
        when {
            "postgresql" in productName -> PostgresDialect
            // MySQL will be added in Phase 2
            else -> throw UnsupportedDatabaseException(conn.metaData.databaseProductName)
        }
    }
