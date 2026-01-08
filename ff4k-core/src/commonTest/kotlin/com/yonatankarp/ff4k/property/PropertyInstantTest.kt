package com.yonatankarp.ff4k.property

import kotlinx.datetime.Instant
import kotlin.test.assertTrue

/**
 * Tests for PropertyInstant class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyInstantTest : PropertyContractTest<Instant, PropertyInstant>() {

    override val serializer = PropertyInstant.serializer()

    override fun create(
        name: String,
        value: Instant,
        description: String?,
        fixedValues: Set<Instant>,
        readOnly: Boolean,
    ): PropertyInstant = PropertyInstant(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "createdAt"
    override fun sampleValue(): Instant = Instant.parse("2024-01-15T10:30:00Z")

    override fun otherValueNotInFixedValues(): Instant = Instant.parse("2024-10-01T00:00:00Z")

    override fun fixedValuesIncludingSample(sample: Instant): Set<Instant> = setOf(
        Instant.parse("2024-06-15T00:00:00Z"),
        Instant.parse("2024-12-25T10:00:00Z"),
        Instant.parse("2025-06-15T00:00:00Z"),
        sample,
    )

    override fun assertJsonHasValue(
        jsonString: String,
        expectedValue: Instant,
    ) {
        assertTrue(
            jsonString.contains(""""value":"$expectedValue""""),
            "JSON missing Instant value: $jsonString",
        )
    }
}
