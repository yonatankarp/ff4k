package com.yonatankarp.ff4k.property

import kotlin.test.assertTrue

/**
 * Tests for PropertyString class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyStringTest : PropertyContractTest<String, PropertyString>() {

    override val serializer = PropertyString.serializer()

    override fun create(
        name: String,
        value: String,
        description: String?,
        fixedValues: Set<String>,
        readOnly: Boolean,
    ): PropertyString = PropertyString(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "apiKey"
    override fun sampleValue(): String = "secret123"
    override fun otherValueNotInFixedValues(): String = "invalid"
    override fun fixedValuesIncludingSample(sample: String): Set<String> = setOf("dev", "staging", "prod", sample)

    override fun assertJsonHasValue(jsonString: String, expectedValue: String) {
        assertTrue(""""value":"$expectedValue"""" in jsonString, "JSON missing value: $jsonString")
    }
}
