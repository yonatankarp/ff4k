# Basics

## Creating an instance

`FF4k` holds a `FeatureStore` and a `PropertyStore`. Both default to in-memory stores.

```kotlin
val ff4k = FF4k()

// or with explicit stores
val ff4k = FF4k(
    features = SqliteFeatureStore(driver),
    properties = SqlitePropertyStore(driver),
)

// or from a configuration
val ff4k = FF4k(FF4kConfiguration.fromJson(json))
```

## Features

A `Feature` is a data class. Use named arguments and `copy` instead of builders:

```kotlin
val feature = Feature(
    id = "dark-mode",
    enabled = true,
    description = "Enable dark mode theme",
    group = "ui",
    permissions = setOf("ADMIN"),
    strategy = PercentageStrategy(50),
    properties = listOf(Property("contrast", 0.8)),
)

ff4k.features.put(feature)                       // insert or replace
ff4k.features.get("dark-mode")                   // Feature? (null when missing)
ff4k.features.getAll()                           // List<Feature>
ff4k.features.update("dark-mode") { it.copy(description = "New") }
ff4k.features.delete("dark-mode")
```

`update` is atomic: the transform runs against the latest stored value and the result is written back, so concurrent updates never lose changes.

## Checking a flag

```kotlin
if (ff4k.check("dark-mode")) { ... }
```

`check` returns true when the feature exists, is enabled and its strategy, if any, accepts the context. Unknown features are reported as disabled.

Strategies read values from an evaluation context, a plain `Map<String, Any>`:

```kotlin
ff4k.check("beta-checkout", mapOf("userId" to user.id, "region" to user.region))
```

## Toggling and groups

Extension functions on `FeatureStore` cover the common edits:

```kotlin
ff4k.features.enable("dark-mode")
ff4k.features.disable("dark-mode")
ff4k.features.group("ui")           // List<Feature>
ff4k.features.enableGroup("ui")
ff4k.features.disableGroup("ui")
```

Anything else is a `copy` inside `update`:

```kotlin
ff4k.features.update("dark-mode") { it.copy(permissions = it.permissions + "BETA") }
```

## Caching

`check` reads the feature on every call. In-memory and SQLite stores make that cheap; a backend database does not. Wrap any store with `cached()` to serve reads from a snapshot refreshed every `ttl` (30 seconds by default). Writes made through the cached store go to the backend and invalidate the snapshot immediately; writes made elsewhere become visible after the ttl.

```kotlin
val ff4k = FF4k(
    features = JdbcFeatureStore(dataSource, JdbcDialect.Postgres).cached(ttl = 10.seconds),
    properties = JdbcPropertyStore(dataSource, JdbcDialect.Postgres).cached(),
)
```
