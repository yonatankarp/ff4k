package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyInt class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyIntTest : PropertyContractTest<Int, PropertyInt>() {

    override val serializer = PropertyInt.serializer()

    override fun create(
        name: String,
        value: Int,
        description: String?,
        fixedValues: Set<Int>,
        readOnly: Boolean
    ): PropertyInt = PropertyInt(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "maxRetries"
    override fun sampleValue(): Int = 3
    override fun otherValueNotInFixedValues(): Int = 5
    override fun fixedValuesIncludingSample(sample: Int): Set<Int> =
        setOf(1, 2, 3, sample)
}
