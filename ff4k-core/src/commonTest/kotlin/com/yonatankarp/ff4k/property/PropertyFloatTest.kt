package com.yonatankarp.ff4k.property

/**
 * Tests for PropertyFloat class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyFloatTest : PropertyContractTest<Float, PropertyFloat>() {

    override val serializer = PropertyFloat.serializer()

    override fun create(
        name: String,
        value: Float,
        description: String?,
        fixedValues: Set<Float>,
        readOnly: Boolean,
    ): PropertyFloat = PropertyFloat(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "temperature"
    override fun sampleValue(): Float = 98.6f
    override fun otherValueNotInFixedValues(): Float = 95.0f

    override fun fixedValuesIncludingSample(sample: Float): Set<Float> = setOf(98.6f, 100.4f, 102.2f, sample)
}
