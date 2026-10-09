# SQLite Store

`ff4k-store-sqlite` provides `SqliteFeatureStore` and `SqlitePropertyStore` on top of [SQLDelight](https://cashapp.github.io/sqldelight/). Features and properties are stored as JSON documents keyed by id and name; feature updates use optimistic locking with retry.

## Installation

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-sqlite:<version>")
    implementation("app.cash.sqldelight:sqlite-driver:<sqldelight-version>")
}
```

## Usage

```kotlin
val driver = JdbcSqliteDriver("jdbc:sqlite:ff4k.db")
SqliteDatabase.Schema.create(driver).await()

val ff4k = FF4k(
    features = SqliteFeatureStore(driver),
    properties = SqlitePropertyStore(driver),
)
```

Both stores share one schema, so create it once per database.

## Custom strategies and property types

Pass a `Json` built on `ff4kSerializersModule` to persist your own strategies and property value types:

```kotlin
val json = Json {
    serializersModule = ff4kSerializersModule + SerializersModule {
        polymorphic(FlippingStrategy::class) { subclass(MyStrategy::class) }
        polymorphic(Any::class) { subclass(MyValueType::class) }
    }
}
val featureStore = SqliteFeatureStore(driver, json)
```
