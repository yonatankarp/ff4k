# Packages

| Package                  | Description                                                                                                                                      | Platforms |
|:-------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------|:----------|
| **`ff4k-bom`**           | Bill of Materials: import it to keep every FF4K module on the same version.                                                                      | Maven     |
| **`ff4k-core`**          | The API (`FF4k`, `Feature`, `Property`, `FlippingStrategy`), in-memory stores, built-in strategies and JSON configuration.                        | JVM       |
| **`ff4k-store-jdbc`**    | `FeatureStore` and `PropertyStore` for backend databases on any `DataSource`, with PostgreSQL and MySQL dialects.                                | JVM       |
| **`ff4k-store-mongodb`** | `FeatureStore` and `PropertyStore` for MongoDB on the official Kotlin coroutine driver.                                                           | JVM       |
| **`ff4k-store-sqlite`**  | `FeatureStore` and `PropertyStore` backed by SQLite through SQLDelight.                                                                           | JVM       |
| **`ff4k-contract-test`** | Kotest contract suites (`FeatureStoreContractTest`, `PropertyStoreContractTest`) to verify custom store implementations.                          | JVM       |

All modules are Kotlin Multiplatform with common code; the JVM target is the only one published today.
