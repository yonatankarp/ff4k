package com.yonatankarp.ff4k.property

import kotlinx.datetime.LocalDate
import kotlin.test.assertTrue

/**
 * Tests for PropertyLocalDate class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyLocalDateTest : PropertyContractTest<LocalDate, PropertyLocalDate>() {

    override val serializer = PropertyLocalDate.serializer()

    override fun create(
        name: String,
        value: LocalDate,
        description: String?,
        fixedValues: Set<LocalDate>,
        readOnly: Boolean
    ): PropertyLocalDate = PropertyLocalDate(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "releaseDate"
    override fun sampleValue(): LocalDate = LocalDate.parse("2024-01-15")

    override fun otherValueNotInFixedValues(): LocalDate =
        LocalDate.parse("2024-12-31")

    override fun fixedValuesIncludingSample(sample: LocalDate): Set<LocalDate> =
        setOf(
            LocalDate.parse("2024-12-24"),
            LocalDate.parse("2024-12-25"),
            LocalDate.parse("2024-12-26"),
            sample,
        )

    override fun assertJsonHasValue(jsonString: String, expectedValue: LocalDate) {
        assertTrue(
            jsonString.contains(""""value":"${expectedValue.toString()}""""),
            "JSON missing LocalDate value: $jsonString"
        )
    }
}
