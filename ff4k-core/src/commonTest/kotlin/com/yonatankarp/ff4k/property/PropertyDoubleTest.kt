package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyDouble class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyDoubleTest : PropertyContractTest<Double, PropertyDouble>() {

    override val serializer = PropertyDouble.serializer()

    override fun create(
        name: String,
        value: Double,
        description: String?,
        fixedValues: Set<Double>,
        readOnly: Boolean
    ): PropertyDouble = PropertyDouble(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "rate"
    override fun sampleValue(): Double = 0.05
    override fun otherValueNotInFixedValues(): Double = 0.15

    override fun fixedValuesIncludingSample(sample: Double): Set<Double> =
        setOf(0.01, 0.05, 0.1, sample)
}
