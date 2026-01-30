package com.yonatankarp.ff4k.property

import com.yonatankarp.ff4k.property.PropertyLogLevel.LogLevel
import com.yonatankarp.ff4k.test.contract.property.PropertyContractTest
import io.kotest.matchers.string.shouldContain

/**
 * Tests for PropertyLogLevel class.
 *
 * @author Yonatan Karp-Rudin
 */
internal class PropertyLogLevelTest : PropertyContractTest<LogLevel, PropertyLogLevel>() {

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
        jsonString shouldContain """"value":"${expectedValue.name}""""
    }
}
