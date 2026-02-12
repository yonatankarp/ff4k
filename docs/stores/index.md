# Feature Stores

FF4K supports multiple storage backends for feature flags.

| Store               | Module              | Platforms                | Supported Databases |
|---------------------|---------------------|--------------------------|---------------------|
| [JDBC](jdbc.md)     | `ff4k-store-jdbc`   | JVM                      | PostgreSQL, MySQL   |
| [SQLite](sqlite.md) | `ff4k-store-sqlite` | JVM, Android, iOS, Native | SQLite              |

See [database requirements](../packages.md#database-requirements) for minimum versions.
