package com.yonatankarp.ff4k.property

import kotlinx.datetime.LocalDateTime
import kotlin.test.assertTrue

/**
 * Tests for PropertyLocalDateTime class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyLocalDateTimeTest :
    PropertyContractTest<LocalDateTime, PropertyLocalDateTime>() {

    override val serializer = PropertyLocalDateTime.serializer()

    override fun create(
        name: String,
        value: LocalDateTime,
        description: String?,
        fixedValues: Set<LocalDateTime>,
        readOnly: Boolean
    ): PropertyLocalDateTime = PropertyLocalDateTime(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "scheduledAt"
    override fun sampleValue(): LocalDateTime =
        LocalDateTime.parse("2024-01-15T10:30:00")

    override fun otherValueNotInFixedValues(): LocalDateTime =
        LocalDateTime.parse("2026-12-25T10:00:00")

    override fun fixedValuesIncludingSample(sample: LocalDateTime): Set<LocalDateTime> =
        setOf(
            LocalDateTime.parse("2024-12-25T10:00:00"),
            LocalDateTime.parse("2024-12-25T14:00:00"),
            LocalDateTime.parse("2024-12-25T18:00:00"),
            sample,
        )

    override fun assertJsonHasValue(
        jsonString: String,
        expectedValue: LocalDateTime
    ) {
        assertTrue(
            jsonString.contains(""""value":"$expectedValue""""),
            "JSON missing LocalDateTime value: $jsonString"
        )
    }
}
