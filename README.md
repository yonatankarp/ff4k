# FF4K - Feature Flags for Kotlin

<div align="center">

[![CI](https://github.com/yonatankarp/ff4k/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/yonatankarp/ff4k/actions/workflows/ci.yml)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://yonatankarp.github.io/ff4k/)
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-17-orange.svg?logo=openjdk)](https://openjdk.org/)
[![GitHub release](https://img.shields.io/github/v/release/yonatankarp/ff4k)](https://github.com/yonatankarp/ff4k/releases)
[![CodeRabbit Reviews](https://img.shields.io/coderabbit/prs/github/yonatankarp/ff4k?utm_source=oss&utm_medium=github&utm_campaign=yonatankarp%2Fff4k&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)](https://coderabbit.ai)

### Code Quality

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=coverage)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=bugs)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_ff4k&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=yonatankarp_ff4k)

</div>

FF4K is a Kotlin Multiplatform port of the ideas behind [FF4J](https://ff4j.org/) (Feature Flipping for Java): feature flags, flipping strategies and typed properties behind a small, coroutine-first API. The JVM target ships today; other Kotlin targets can be added from the same common code.

> **Alpha.** FF4K is pre-1.0. Public APIs, the JSON format and store schemas change between releases without a deprecation cycle.

## Installation

```kotlin
dependencies {
    implementation(platform("com.yonatankarp:ff4k-bom:<version>"))
    implementation("com.yonatankarp:ff4k-core")
}
```

## Usage

```kotlin
import com.yonatankarp.ff4k.*
import com.yonatankarp.ff4k.strategy.*

val ff4k = FF4k()

ff4k.features.put(Feature("dark-mode", enabled = true, group = "ui"))
ff4k.features.put(
    Feature(
        id = "beta-checkout",
        enabled = true,
        strategy = ContextFilterStrategy("region", setOf("eu")) and UserPercentageStrategy(10),
    ),
)
ff4k.properties.put(Property("max-retries", 3))

if (ff4k.check("dark-mode")) enableDarkMode()
ff4k.check("beta-checkout", mapOf("region" to "eu", "userId" to user.id))

val retries: Int? = ff4k.property("max-retries")

ff4k.features.disableGroup("ui")
```

Features and properties can also be loaded from JSON:

```kotlin
val ff4k = FF4k(FF4kConfiguration.fromJson(File("ff4k.json").readText()))
```

```json
{
  "features": [
    { "id": "dark-mode", "enabled": true, "group": "ui" },
    { "id": "beta-checkout", "enabled": true,
      "strategy": { "type": "contextFilter", "key": "region", "allowed": ["eu"] } }
  ],
  "properties": [
    { "name": "max-retries", "type": "int", "value": 3 }
  ]
}
```

Stores are pluggable: `ff4k-core` ships in-memory stores, `ff4k-store-jdbc` persists to PostgreSQL or MySQL on any `DataSource`, `ff4k-store-mongodb` to MongoDB, and `ff4k-store-sqlite` to SQLite through SQLDelight. Wrap a database store with `cached()` to keep flag checks off the database. Implement `FeatureStore` and `PropertyStore` for anything else and verify it with `ff4k-contract-test`.

See the [documentation](https://yonatankarp.github.io/ff4k/) for strategies, configuration and custom stores.

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a pull request.

## Security

To report a security vulnerability, please see our [Security Policy](SECURITY.md).

## License

This project is licensed under the [Apache License 2.0](LICENSE).
