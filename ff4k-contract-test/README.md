# FF4K Contract Test Module

The `ff4k-contract-test` module provides reusable contract tests for verifying
implementations of extensible FF4K functionality. This module serves as a
comprehensive test suite that ensures consistency and correctness across both
internal modules and external extensions.

## Purpose

This module exists to:

- **Ensure Consistency**: All implementations of FF4K interfaces follow the same behavioral contracts
- **Enable Extensibility**: Both internal modules and external developers can verify their implementations using standardized test suites
- **Reduce Duplication**: Eliminates the need to write repetitive tests for common functionality
- **Maintain Quality**: Provides comprehensive test coverage that all implementations must pass
- **Support Multiple Platforms**: Built with Kotlin Multiplatform to work across JVM, Android, and Native targets

## Who Should Use This Module

### Internal Module Development
When developing new modules within the FF4K project that implement core
interfaces, use the contract tests to ensure your implementation meets the
required standards.

### External Library Extensions
When extending FF4K with custom implementations (e.g., custom property types,
storage backends, or feature flags), use these contract tests to verify your
implementation is correct and compatible with the FF4K ecosystem.

## Available Contract Tests

### PropertyContractTest

Tests for implementations of the `Property<V>` interface. See the [source code](src/commonMain/kotlin/com/yonatankarp/ff4k/test/contract/property/PropertyContractTest.kt)
for the complete test contract.

**Example usage:**
```kotlin
class PropertyStringTest : PropertyContractTest<String, PropertyString>() {
    override val serializer = PropertyString.serializer()

    override fun create(
        name: String,
        value: String,
        description: String?,
        fixedValues: Set<String>,
        readOnly: Boolean
    ) = PropertyString(name, value, description, fixedValues, readOnly)

    override fun sampleName() = "myProperty"
    override fun sampleValue() = "hello"
    override fun otherValueNotInFixedValues() = "world"
    override fun fixedValuesIncludingSample(sample: String) = setOf(sample, "other")
}
```

### FeatureStoreContractTest

Tests for implementations of the `FeatureStore` interface. This comprehensive test suite verifies all feature store operations including CRUD, group management, permissions, and edge cases. See the [source code](src/commonMain/kotlin/com/yonatankarp/ff4k/test/contract/store/FeatureStoreContractTest.kt)
for the complete test contract.

**Example usage:**
```kotlin
class InMemoryFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = InMemoryFeatureStore()
}
```

The contract test includes comprehensive coverage for:
- Basic CRUD operations (create, read, update, delete)
- Feature enable/disable operations
- Group management (add to group, remove from group, enable/disable groups)
- Permission operations (grant/revoke roles)
- Operator overloading (`in`, `+=`, `-=`, `[]`)
- Extension functions (toggle, createOrUpdate, updateFeature, etc.)
- Edge cases and exception handling

_More contract tests will be added as the library grows._

## Usage

### Add the Dependency

**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    testImplementation("com.yonatankarp:ff4k-contract-test:<version>")
}
```

**Gradle (Groovy DSL)**
```groovy
dependencies {
    testImplementation 'com.yonatankarp:ff4k-contract-test:<version>'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.yonatankarp</groupId>
    <artifactId>ff4k-contract-test</artifactId>
    <version>${ff4k.version}</version>
    <scope>test</scope>
</dependency>
```

### Implement Your Tests

Extend the appropriate contract test class and implement the required abstract methods. The contract tests will automatically verify that your implementation meets FF4K standards.

For detailed examples, see how FF4K core uses these tests:

- **Property implementations**: See `ff4k-core/src/commonTest/kotlin/com/yonatankarp/ff4k/property/`
  - `PropertyStringTest.kt`
  - `PropertyIntTest.kt`
  - `PropertyBooleanTest.kt`
  - `PropertyBigDecimalTest.kt`
  - And more...

These examples demonstrate how to extend the contract tests for your own implementations.

## Multiplatform Support

This module is built with Kotlin Multiplatform and supports:
- JVM
- Android
- iOS
- Native (Linux, Windows, macOS)

The contract tests work consistently across all supported platforms.

## Contributing New Contract Tests

When adding new extensible functionality to FF4K, consider adding contract tests to this module to ensure consistent behavior across implementations.

## License

This module is part of FF4K and is licensed under the same license as the parent project.
