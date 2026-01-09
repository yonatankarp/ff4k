package com.yonatankarp.ff4k.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property implementation for log level values.
 *
 * @property name Unique name of the property
 * @property value Current log level value
 * @property description Optional description
 * @property fixedValues Set of allowed log level values (empty if no restrictions)
 * @property readOnly Whether the property is read-only
 *
 * @author Yonatan Karp-Rudin
 */
@Serializable
@SerialName("logLevel")
data class PropertyLogLevel(
    override val name: String,
    override val value: LogLevel,
    override val description: String? = null,
    override val fixedValues: Set<LogLevel> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<PropertyLogLevel.LogLevel> {
    enum class LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
    }

    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }
}
