package com.yonatankarp.ff4k

/**
 * A named, typed value. Supported value types for JSON serialization are String, Int, Long, Double,
 * Boolean, Instant, LocalDate and LocalDateTime.
 */
data class Property<out T : Any>(
    val name: String,
    val value: T,
    val description: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Property name cannot be blank" }
    }
}
