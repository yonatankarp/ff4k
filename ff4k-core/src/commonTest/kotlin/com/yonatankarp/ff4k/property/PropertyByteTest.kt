package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyByte class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyByteTest : PropertyContractTest<Byte, PropertyByte>() {

    override val serializer = PropertyByte.serializer()

    override fun create(
        name: String,
        value: Byte,
        description: String?,
        fixedValues: Set<Byte>,
        readOnly: Boolean,
    ): PropertyByte = PropertyByte(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "flag"
    override fun sampleValue(): Byte = 127
    override fun otherValueNotInFixedValues(): Byte = 5

    override fun fixedValuesIncludingSample(sample: Byte): Set<Byte> = setOf(0, 1, 2, sample)
}
