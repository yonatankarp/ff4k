package com.yonatankarp.ff4k.property

import com.yonatankarp.ff4k.property.PropertyLogLevel.LogLevel
import kotlin.test.assertTrue

/**
 * Tests for PropertyLogLevel class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyLogLevelTest : PropertyContractTest<LogLevel, PropertyLogLevel>() {

    override val serializer = PropertyLogLevel.serializer()

    override fun create(
        name: String,
        value: LogLevel,
        description: String?,
        fixedValues: Set<LogLevel>,
        readOnly: Boolean,
    ): PropertyLogLevel = PropertyLogLevel(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "log.level"
    override fun sampleValue(): LogLevel = LogLevel.INFO

    override fun otherValueNotInFixedValues(): LogLevel = LogLevel.FATAL

    override fun fixedValuesIncludingSample(sample: LogLevel): Set<LogLevel> = setOf(
        LogLevel.TRACE,
        LogLevel.DEBUG,
        LogLevel.INFO,
        LogLevel.WARN,
        LogLevel.ERROR,
        sample,
    )

    override fun assertJsonHasValue(jsonString: String, expectedValue: LogLevel) {
        assertTrue(
            jsonString.contains(""""value":"${expectedValue.name}""""),
            "JSON missing log level value: $jsonString",
        )
    }
}
