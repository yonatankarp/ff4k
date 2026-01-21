# Properties

Properties in FF4K allow you to manage configuration values alongside your feature flags. Unlike feature flags which are boolean, properties can hold values of various types.

## Built-in Property Types

FF4K comes with support for common data types out of the box:

- `PropertyString`
- `PropertyInt`
- `PropertyLong`
- `PropertyDouble`
- `PropertyFloat`
- `PropertyBoolean`
- `PropertyByte`
- `PropertyShort`
- `PropertyBigInteger`
- `PropertyBigDecimal`
- `PropertyLocalDate`
- `PropertyLocalDateTime`
- `PropertyInstant`
- `PropertyLogLevel`

## Usage

### Defining Properties in DSL

You can define properties within the `properties` block of the `ff4k` DSL.

```kotlin
suspend fun main() {
    val ff4k = ff4k {
        properties {
            property("app-title") {
                value = "My Awesome App"
            }

            property("max-connections") {
                value = 10
                description = "Maximum number of concurrent connections"
            }
        }
    }
}
```

### Retrieving Properties

To retrieve a property, use the `property` method. You should specify the expected type.

```kotlin
// Get the property object
val titleProp: Property<String>? = ff4k.property<String>("app-title")
println("Title: ${titleProp?.value}")

// Get value directly (safe)
val maxConns: Int? = ff4k.property<Int>("max-connections")?.value
```

## Creating Custom Properties

If the built-in types don't meet your needs, you can create custom property types by implementing the `Property<T>` interface or extending `AbstractProperty<T>`.

### 1. Implement the Interface

You need to implement `Property<T>`. It's recommended to make your implementation a data class and include serialization support.

```kotlin
import com.yonatankarp.ff4k.property.Property
import kotlinx.serialization.Serializable

@Serializable
data class PropertyColor(
    override val name: String,
    override val value: String, // Storing hex code as string
    override val description: String? = null,
    override val fixedValues: Set<String> = emptySet(),
    override val readOnly: Boolean = false
) : Property<String>
```

### 2. Using Custom Properties

You can manually add custom properties to the store. Here is a complete example of how to register and use a custom property type:

```kotlin
suspend fun main() {
    // 1. Initialize your property store
    val myPropertyStore = InMemoryPropertyStore()

    // 2. Initialize FF4K with your store
    val ff4k = ff4k(propertyStore = myPropertyStore) {
        // ... other configuration ...
    }

    // 3. Create your custom property
    val brandColor = PropertyColor(
        name = "brand-color",
        value = "#FF5722",
        description = "Primary brand color"
    )

    // 4. Add it to the store manually
    myPropertyStore += brandColor

    // 5. Retrieve and use it
    val storedColor = ff4k.property<String>("brand-color")
    println("Brand Color: ${storedColor?.value}") // Output: #FF5722
}
```
