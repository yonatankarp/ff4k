# ff4k-store-jdbc

JDBC-based `FeatureStore` for relational databases on the JVM.

See [documentation](../docs/stores/jdbc.md) for supported databases and usage.

## Adding New Database Support

The `SqlDialect` interface is sealed, so new databases must be added to the library.

### 1. Create a Dialect

Add a new dialect in `ff4k-store-sql-common`:

```kotlin
// ff4k-store-sql-common/src/main/kotlin/.../sql/MariaDbDialect.kt
data object MariaDbDialect : SqlDialect {
    override val databaseName = "MariaDB"
    override val createFeatureTableSql = "..."
    override val selectAllFeaturesSql = "..."
    override val selectFeatureByUidSql = "..."
    override val featureExistsSql = "..."
    override val countFeaturesSql = "..."
    override val insertFeatureSql = "..."
    override val updateFeatureSql = "..."
    override val upsertFeatureSql = "..."  // Database-specific syntax
    override val deleteFeatureByUidSql = "..."
    override val deleteAllFeaturesSql = "..."
}
```

Use existing dialects (`PostgresDialect`, `MysqlDialect`) as reference.

### 2. Register Detection

Add detection in `JdbcFeatureStores.kt`:

```kotlin
private fun detectDialect(dataSource: DataSource): SqlDialect =
    dataSource.connection.use { conn ->
        val productName = conn.metaData.databaseProductName.lowercase()
        when {
            "postgresql" in productName -> PostgresDialect
            "mysql" in productName -> MysqlDialect
            "mariadb" in productName -> MariaDbDialect  // Add here
            else -> throw UnsupportedDatabaseException(conn.metaData.databaseProductName)
        }
    }
```

### 3. Add Tests

Create a contract test with Testcontainers:

```kotlin
class MariaDbFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore =
        jdbcFeatureStore(mariadb.toDataSource()).also { it.clear() }

    companion object {
        private val mariadb = MariaDBContainer("mariadb:10.11").apply { start() }
    }
}
```

Add the driver and testcontainers dependencies to `build.gradle.kts`.