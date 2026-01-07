package com.yonatankarp.ff4k.property

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for PropertyBoolean class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBooleanTest : PropertyContractTest<Boolean, PropertyBoolean>() {

    override val serializer = PropertyBoolean.serializer()

    override fun create(
        name: String,
        value: Boolean,
        description: String?,
        fixedValues: Set<Boolean>,
        readOnly: Boolean
    ): PropertyBoolean = PropertyBoolean(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "enableCache"
    override fun sampleValue(): Boolean = true
    override fun otherValueNotInFixedValues(): Boolean = false

    override fun fixedValuesIncludingSample(sample: Boolean): Set<Boolean> =
        setOf(true, sample)

    @Test
    fun `stores false value`() {
        // Given
        val name = "disabled"
        val value = false

        // When
        val property = PropertyBoolean(name = name, value = value)

        // Then
        assertEquals(value, property.value)
    }
}
