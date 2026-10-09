package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.serialization.PropertyListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val id: String,
    val enabled: Boolean = false,
    val description: String? = null,
    val group: String? = null,
    val permissions: Set<String> = emptySet(),
    val strategy: FlippingStrategy? = null,
    @Serializable(with = PropertyListSerializer::class)
    val properties: List<Property<Any>> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Feature id cannot be blank" }
    }

    fun property(name: String): Property<Any>? = properties.firstOrNull { it.name == name }
}
