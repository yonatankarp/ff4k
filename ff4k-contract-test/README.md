# ff4k-contract-test

Kotest contract suites that verify a `FeatureStore` or `PropertyStore` implementation behaves like the built-in ones.

```kotlin
class MyFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = MyFeatureStore()
}

class MyPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = MyPropertyStore()
}
```

`createStore` is called once per test and must return an empty store.

See the [documentation](../docs/customization/testing.md) for the behaviour that is checked.
