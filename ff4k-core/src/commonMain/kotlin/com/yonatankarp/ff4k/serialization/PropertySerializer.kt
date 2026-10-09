package com.yonatankarp.ff4k.serialization

import com.yonatankarp.ff4k.Property
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/**
 * Encodes a property as `{"name", "type", "value", "description"}`.
 *
 * String, Int, Long, Double, Boolean, Instant, LocalDate and LocalDateTime are built in. Any other value type
 * must be a `@Serializable` class registered in the [Json] serializers module under `polymorphic(Any::class)`;
 * its serial name becomes the `type`.
 */
@OptIn(ExperimentalSerializationApi::class)
object PropertySerializer : KSerializer<Property<Any>> {
    @Serializable
    private class Surrogate(
        val name: String,
        val type: String,
        val value: JsonElement,
        val description: String? = null,
    )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Property<Any>,
    ) {
        val json = encoder.json()
        val (type, element) = when (val v = value.value) {
            is String -> "string" to JsonPrimitive(v)

            is Int -> "int" to JsonPrimitive(v)

            is Long -> "long" to JsonPrimitive(v)

            is Double -> "double" to JsonPrimitive(v)

            is Boolean -> "boolean" to JsonPrimitive(v)

            is Instant -> "instant" to JsonPrimitive(v.toString())

            is LocalDate -> "localDate" to JsonPrimitive(v.toString())

            is LocalDateTime -> "localDateTime" to JsonPrimitive(v.toString())

            else -> {
                val serializer = json.serializersModule.getPolymorphic(Any::class, v)
                    ?: throw SerializationException(
                        "No serializer registered for property type ${v::class.simpleName}; " +
                            "register it with polymorphic(Any::class) in the serializers module",
                    )
                serializer.descriptor.serialName to json.encodeToJsonElement(serializer, v)
            }
        }
        encoder.encodeSerializableValue(Surrogate.serializer(), Surrogate(value.name, type, element, value.description))
    }

    override fun deserialize(decoder: Decoder): Property<Any> {
        val json = decoder.json()
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        val value: Any = when (surrogate.type) {
            "string" -> surrogate.value.jsonPrimitive.content

            "int" -> surrogate.value.jsonPrimitive.int

            "long" -> surrogate.value.jsonPrimitive.long

            "double" -> surrogate.value.jsonPrimitive.double

            "boolean" -> surrogate.value.jsonPrimitive.boolean

            "instant" -> Instant.parse(surrogate.value.jsonPrimitive.content)

            "localDate" -> LocalDate.parse(surrogate.value.jsonPrimitive.content)

            "localDateTime" -> LocalDateTime.parse(surrogate.value.jsonPrimitive.content)

            else -> {
                val serializer = json.serializersModule.getPolymorphic(Any::class, serializedClassName = surrogate.type)
                    ?: throw SerializationException("Unsupported property type: ${surrogate.type}")
                json.decodeFromJsonElement(serializer, surrogate.value)
            }
        }
        return Property(surrogate.name, value, surrogate.description)
    }

    private fun Encoder.json(): Json = (this as? JsonEncoder)?.json ?: throw SerializationException("Property can only be encoded as JSON")

    private fun Decoder.json(): Json = (this as? JsonDecoder)?.json ?: throw SerializationException("Property can only be decoded from JSON")
}

object PropertyListSerializer : KSerializer<List<Property<Any>>> by ListSerializer(PropertySerializer)
