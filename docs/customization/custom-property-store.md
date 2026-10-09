# Custom Property Store

`PropertyStore` has four suspend functions:

```kotlin
interface PropertyStore {
    suspend fun get(name: String): Property<Any>?
    suspend fun getAll(): List<Property<Any>>
    suspend fun put(property: Property<Any>)   // insert or replace
    suspend fun delete(name: String)           // no-op when missing
}
```

`Property` is not annotated `@Serializable` because its value is generic. Use `PropertySerializer` (one property) or `PropertyListSerializer` (a list) with any `Json` instance:

```kotlin
val text = ff4kJson.encodeToString(PropertySerializer, property)
val property = ff4kJson.decodeFromString(PropertySerializer, text)
```

Verify your implementation with `PropertyStoreContractTest`, see [Contract Testing](testing.md).
