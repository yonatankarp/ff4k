package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyLong class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyLongTest : PropertyContractTest<Long, PropertyLong>() {

    override val serializer = PropertyLong.serializer()

    override fun create(
        name: String,
        value: Long,
        description: String?,
        fixedValues: Set<Long>,
        readOnly: Boolean,
    ): PropertyLong = PropertyLong(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "timestamp"
    override fun sampleValue(): Long = 1234567890L
    override fun otherValueNotInFixedValues(): Long = 403L

    override fun fixedValuesIncludingSample(sample: Long): Set<Long> = setOf(200L, 404L, 500L, sample)
}
