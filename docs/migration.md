# Migrating from 0.3 to 0.4

0.4 is an alpha with a redesigned, much smaller API. Nothing is deprecated first: every public type, the JSON configuration format and the store schemas change. This page lists what to do.

## Platforms and Kotlin

- 0.4 is published for the **JVM only**. Android and iOS users should stay on 0.3.0; other targets will return when someone needs them.
- The library is built with **Kotlin 2.4**. Consumers on older Kotlin versions may not be able to read its metadata.

## Packages

`Feature`, `Property`, `FlippingStrategy`, `FeatureStore`, `PropertyStore`, `FF4k` and `FF4kConfiguration` now live directly in `com.yonatankarp.ff4k`. The `core`, `dsl`, `property`, `config`, `exception` and `utils` packages are gone.

## Creating features and properties

The builder DSL is removed. Use the data classes with named arguments:

=== "0.3"

    ```kotlin
    val ff4k = ff4k(autoCreate = true) {
        features {
            feature("dark-mode") {
                isEnabled = true
                group = "ui"
                permissions("ADMIN")
            }
        }
        properties {
            property("max-retries") { value = 3 }
        }
    }
    ```

=== "0.4"

    ```kotlin
    val ff4k = FF4k()
    ff4k.features.put(Feature("dark-mode", enabled = true, group = "ui", permissions = setOf("ADMIN")))
    ff4k.properties.put(Property("max-retries", 3))
    ```

| 0.3 | 0.4 |
|-----|-----|
| `feature.uid`, `isEnabled`, `flippingStrategy`, `customProperties` | `id`, `enabled`, `strategy`, `properties` (a list) |
| `PropertyInt`, `PropertyString`, ... | `Property<T>` |
| `ff4k.ifEnabled("f") { }` | `if (ff4k.check("f")) { }` |
| `ff4k.property<Int>("p")?.value` | `ff4k.property<Int>("p")` |
| `ff4k.enableGroup("g")` | `ff4k.features.enableGroup("g")` |
| `autoCreate = true` | Removed; unknown features are reported as disabled |
| `FlippingExecutionContext`, `withFlippingContext` | A plain `Map<String, Any>` passed to `check` |

Float, Short, Byte, BigInteger, BigDecimal and log-level properties, fixed values and read-only properties are removed. Register your own value types as described in [Properties](usage/properties.md).

## Permissions

Permissions are now enforced: a feature with `permissions` is active only when `check` receives a matching role under `FF4k.ROLES`. In 0.3 they were stored but ignored, so features that relied on that will turn off. See [Basics](usage/basics.md#permissions).

## Strategies

| 0.3 | 0.4 |
|-----|-----|
| `AllowListStrategy`, `ClientFilterStrategy`, `ServerFilterStrategy`, `RegionFilterStrategy` | `ContextFilterStrategy(key, allowed)` |
| `DenyListStrategy` | `!ContextFilterStrategy(key, denied)` |
| `PonderationStrategy(0.25)` | `PercentageStrategy(25)` |
| `UserPonderationStrategy(0.25)` | `UserPercentageStrategy(25)` |
| `AlwaysTrueFlippingStrategy` / `AlwaysFalseFlippingStrategy` | No strategy / `enabled = false` |
| `evaluate(featureId, store, context)` | `evaluate(feature, context)` |

`ReleaseDateStrategy`, `DateRangeStrategy` (now `start`/`end`), `DailyHoursStrategy`, `WeekdayStrategy` (now `days`) and `and`/`or`/`!` keep working.

## Stores

`FeatureStore` and `PropertyStore` shrank to `get`, `getAll`, `put`, `update` (features only) and `delete`. `put` is an insert-or-replace; `+=`, `-=`, `createOrUpdate` and the `*AlreadyExists` exceptions are gone. See [Custom Feature Store](customization/custom-feature-store.md).

## JSON configuration

Features and properties are lists instead of maps, and fields are renamed:

```json
{
  "features": [
    { "id": "dark-mode", "enabled": true,
      "strategy": { "type": "contextFilter", "key": "region", "allowed": ["eu"] } }
  ],
  "properties": [
    { "name": "max-retries", "type": "int", "value": 3 }
  ]
}
```

`JsonFF4kConfigurationParser` is replaced by `FF4kConfiguration.fromJson` and `toJson`. See [Configuration](usage/configuration.md).

## Persistent stores

- **SQLite:** the schema changed to one JSON document per row. `SqliteDatabase.Schema.create` fails on a 0.3 database because the `features` table already exists with other columns. Start from a new database file, or drop the `features` and `properties` tables and re-import your flags.
- **JDBC:** `ff4k-store-jdbc` keeps its Maven coordinates but is a new library. It takes a `DataSource` and a `JdbcDialect`, stores both features and properties, and uses new tables named `ff4k_features` and `ff4k_properties`. See [JDBC Store](stores/jdbc.md).
- **R2DBC:** the empty `ff4k-store-r2dbc` module is no longer published.
