package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.serialization.PropertyListSerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import kotlinx.serialization.Serializable

/** Declarative set of features and properties, loadable from JSON. */
@Serializable
data class FF4kConfiguration(
    val features: List<Feature> = emptyList(),
    @Serializable(with = PropertyListSerializer::class)
    val properties: List<Property<Any>> = emptyList(),
) {
    fun toJson(): String = ff4kJson.encodeToString(this)

    companion object {
        fun fromJson(json: String): FF4kConfiguration = ff4kJson.decodeFromString(json)
    }
}
