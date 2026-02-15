package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.store.sql.PostgresDialect
import com.yonatankarp.ff4k.store.sql.SqlDialect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.modules.SerializersModule
import java.util.Locale
import javax.sql.DataSource

private val SUPPORTED_DATABASES = listOf("PostgreSQL")

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
 * val customModule = SerializersModule {
 *     polymorphic(FlippingStrategy::class) {
 *         subclass(MyCustomStrategy::class, MyCustomStrategy.serializer())
 *     }
 * }
 * val store = jdbcFeatureStore(dataSource, customModule)
 * ```
 *
 * @param dataSource The JDBC [DataSource] for database connections.
 * @param customSerializersModule Optional serializers for custom types.
 * @param ioDispatcher The [CoroutineDispatcher] to use for blocking JDBC operations.
 *   Defaults to [Dispatchers.IO].
 * @throws UnsupportedDatabaseException If the database type is not supported.
 */
suspend fun jdbcFeatureStore(
    dataSource: DataSource,
    customSerializersModule: SerializersModule = SerializersModule {},
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): FeatureStore {
    val dialect = withContext(ioDispatcher) { detectDialect(dataSource) }
    return jdbcFeatureStore(dataSource, dialect, customSerializersModule, ioDispatcher)
}

/**
 * Creates a JDBC-backed [FeatureStore] with an explicit SQL dialect.
 *
 * Use this when automatic detection doesn't work (e.g., database proxies).
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

private fun detectDialect(dataSource: DataSource): SqlDialect =
    dataSource.connection.use { conn ->
        val rawProductName = conn.metaData.databaseProductName
        val productName = rawProductName.lowercase(Locale.ROOT)
        when {
            "postgresql" in productName -> PostgresDialect
            else -> throw UnsupportedDatabaseException(
                databaseProductName = rawProductName,
                supported = SUPPORTED_DATABASES,
            )
        }
    }
