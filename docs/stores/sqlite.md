# SQLite Feature Store

The `ff4k-store-sqlite` module provides a SQLite-based `FeatureStore` and `PropertyStore`
implementation using [SQLDelight](https://cashapp.github.io/sqldelight/).

## Supported Platforms

- JVM
- Android
- iOS / Native

## Installation

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-sqlite:<version>")
}
```

## Usage

### JVM

```kotlin
val driver = JdbcSqliteDriver("jdbc:sqlite:ff4k.db")
SqliteDatabase.Schema.create(driver).await()

val featureStore = SqliteFeatureStore(driver)
val propertyStore = SqlitePropertyStore(driver)
```

### Android

```kotlin
val driver = AndroidSqliteDriver(
    schema = SqliteDatabase.Schema,
    context = context,
    name = "ff4k.db"
)

val featureStore = SqliteFeatureStore(driver)
val propertyStore = SqlitePropertyStore(driver)
```

### iOS / Native

```kotlin
val driver = NativeSqliteDriver(SqliteDatabase.Schema, "ff4k.db")

val featureStore = SqliteFeatureStore(driver)
val propertyStore = SqlitePropertyStore(driver)
```

### Custom Serializers

For custom `FlippingStrategy` or `Property` implementations:

```kotlin
val customModule = SerializersModule {
    polymorphic(FlippingStrategy::class) {
        subclass(MyCustomStrategy::class)
    }
}

val featureStore = SqliteFeatureStore(driver, customModule)
val propertyStore = SqlitePropertyStore(driver, customModule)
```

## Features

- **Multiplatform** - JVM, Android, iOS, and Native from a single module
- **Automatic schema creation** - Tables and indexes created via SQLDelight migrations
- **Optimistic locking** - Safe concurrent updates with retry
- **Property store** - Stores both feature flags and typed properties