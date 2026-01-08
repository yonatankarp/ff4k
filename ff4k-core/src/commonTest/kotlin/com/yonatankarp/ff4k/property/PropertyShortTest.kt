package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyShort class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyShortTest : PropertyContractTest<Short, PropertyShort>() {

    override val serializer = PropertyShort.serializer()

    override fun create(
        name: String,
        value: Short,
        description: String?,
        fixedValues: Set<Short>,
        readOnly: Boolean,
    ): PropertyShort = PropertyShort(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "port"
    override fun sampleValue(): Short = 8080
    override fun otherValueNotInFixedValues(): Short = 9000
    override fun fixedValuesIncludingSample(sample: Short): Set<Short> = setOf(80, 443, 8080, sample)
}
