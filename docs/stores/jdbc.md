# JDBC Feature Store

The `ff4k-store-jdbc` module provides a JDBC-based `FeatureStore` implementation for
relational databases on the JVM.

## Supported Databases

- PostgreSQL
- MySQL

## Installation

Add the dependency and your database driver:

```kotlin
dependencies {
    implementation("com.yonatankarp:ff4k-store-jdbc:<version>")

    // Database driver (choose one)
    runtimeOnly("org.postgresql:postgresql:<version>")
    runtimeOnly("com.mysql:mysql-connector-j:<version>")
}
```

## Usage

### Basic Usage

Pass a `DataSource` to create a store. The database type is detected automatically:

```kotlin
val dataSource = HikariDataSource(config)
val store = jdbcFeatureStore(dataSource)

store += Feature(uid = "my-feature", isEnabled = true)
```

### Explicit Dialect

For database proxies or when auto-detection fails, use `<DatabaseName>Dialect`:

```kotlin
val store = jdbcFeatureStore(dataSource, PostgresDialect)
```

### Custom Serializers

For custom `FlippingStrategy` or `Property` implementations:

```kotlin
val customModule = SerializersModule {
    polymorphic(FlippingStrategy::class) {
        subclass(MyCustomStrategy::class)
    }
}

val store = jdbcFeatureStore(dataSource, customModule)
```

## Features

- **Automatic schema creation** - Table created on first use
- **Optimistic locking** - Safe concurrent updates with retry
- **Connection pooling** - Bring your own `DataSource` (HikariCP recommended)