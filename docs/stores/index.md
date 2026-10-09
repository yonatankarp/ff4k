# Feature Stores

`ff4k-core` ships `InMemoryFeatureStore` and `InMemoryPropertyStore`. Persistent stores live in their own modules so you only pull the driver you need:

| Store               | Module              | Databases         | Platforms |
|---------------------|---------------------|-------------------|-----------|
| [JDBC](jdbc.md)     | `ff4k-store-jdbc`   | PostgreSQL, MySQL, any `JdbcDialect` | JVM |
| [SQLite](sqlite.md) | `ff4k-store-sqlite` | SQLite (SQLDelight) | JVM, portable to other Kotlin targets |

Every persistent store is verified by the same [contract tests](../customization/testing.md). Wrap any store with [`cached()`](../usage/basics.md#caching) to avoid a round trip per flag check.

To write your own, see [Custom Feature Store](../customization/custom-feature-store.md).
