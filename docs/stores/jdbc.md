# JDBC Store

`ff4k-store-jdbc` provides `JdbcFeatureStore` and `JdbcPropertyStore` for backend databases on top of any `javax.sql.DataSource`. PostgreSQL and MySQL dialects are built in; implement `JdbcDialect` for another database. Features and properties are stored as JSON documents in `ff4k_features` and `ff4k_properties`; feature updates use optimistic locking with retry.

Calls are suspend functions that run the blocking JDBC work on `Dispatchers.IO`.

## Installation

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-jdbc:<version>")
    implementation("org.postgresql:postgresql:<driver-version>")   // or com.mysql:mysql-connector-j
}
```

## Usage

```kotlin
val dataSource: DataSource = HikariDataSource(config)   // any pooled DataSource
JdbcSchema.create(dataSource)                            // CREATE TABLE IF NOT EXISTS, once at startup

val ff4k = FF4k(
    features = JdbcFeatureStore(dataSource, JdbcDialect.Postgres).cached(),
    properties = JdbcPropertyStore(dataSource, JdbcDialect.Postgres).cached(),
)
```

Wrap the stores with [`cached()`](../usage/basics.md#caching) so flag checks do not hit the database on every call.

| Dialect                | Minimum version | Notes                                   |
|------------------------|-----------------|-----------------------------------------|
| `JdbcDialect.Postgres` | 9.5             | `ON CONFLICT` upsert                    |
| `JdbcDialect.Mysql`    | 8.0.19          | row alias syntax in `ON DUPLICATE KEY`  |

## Other databases

Only the upsert statement differs between databases:

```kotlin
object H2Dialect : JdbcDialect {
    override fun upsert(table: String) =
        "MERGE INTO $table (id, data, version) KEY (id) VALUES (?, ?, 1)"
}
```

## Custom strategies and property types

Pass a `Json` built on `ff4kSerializersModule`, exactly as for the [SQLite store](sqlite.md#custom-strategies-and-property-types).
