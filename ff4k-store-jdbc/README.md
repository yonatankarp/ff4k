# ff4k-store-jdbc

JDBC-based `FeatureStore` for relational databases on the JVM.

See [documentation](../docs/stores/jdbc.md) for supported databases and usage.

## Adding New Database Support

### 1. Create a Base Statements Class

Add an abstract base class in `ff4k-store-sql-common` with the database-specific SQL:

```kotlin
// ff4k-store-sql-common/src/main/kotlin/.../sql/BaseMariaDbStatements.kt
abstract class BaseMariaDbStatements : SqlStatements {
    override val databaseName = "MariaDB"
    override val schemaSql: List<String> = listOf("...")
    override val selectAllFeaturesSql by lazy { "..." }
    // ... other SQL properties using marker() for placeholders
}
```

Use existing base classes (`BasePostgresStatements`, `BaseMysqlStatements`) as reference.

### 2. Create a JDBC Dialect

Add a dialect in `ff4k-store-jdbc` that extends the base class and implements `SqlDialect`:

```kotlin
// ff4k-store-jdbc/src/main/kotlin/.../jdbc/JdbcMariaDbDialect.kt
data object JdbcMariaDbDialect : BaseMariaDbStatements(), SqlDialect {
    override fun marker(_: Int): String = "?"
    override fun isUniqueConstraintViolation(e: java.sql.SQLException): Boolean =
        e.sqlState == "23000"
}
```

### 3. Register Detection

Add detection in `JdbcFeatureStores.kt`:

```kotlin
private fun detectDialect(dataSource: DataSource): SqlDialect =
    dataSource.connection.use { conn ->
        val productName = conn.metaData.databaseProductName.lowercase()
        when {
            "postgresql" in productName -> JdbcPostgresDialect
            "mysql" in productName -> JdbcMysqlDialect
            "mariadb" in productName -> JdbcMariaDbDialect  // Add here
            else -> throw UnsupportedDatabaseException(conn.metaData.databaseProductName)
        }
    }
```

### 4. Add Tests

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
