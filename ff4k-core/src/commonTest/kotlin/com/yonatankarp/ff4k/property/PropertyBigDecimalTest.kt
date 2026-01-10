package com.yonatankarp.ff4k.property

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import com.yonatankarp.ff4k.test.contract.property.PropertyContractTest
import kotlinx.serialization.json.Json
import kotlin.test.assertTrue

/**
 * Tests for PropertyBigDecimal class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBigDecimalTest : PropertyContractTest<BigDecimal, PropertyBigDecimal>() {

    override val json: Json = Json {
        serializersModule = humanReadableSerializerModule
    }

    override val serializer = PropertyBigDecimal.serializer()

    override fun create(
        name: String,
        value: BigDecimal,
        description: String?,
        fixedValues: Set<BigDecimal>,
        readOnly: Boolean,
    ): PropertyBigDecimal = PropertyBigDecimal(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )

    override fun sampleName(): String = "preciseValue"
    override fun sampleValue(): BigDecimal = "123.456789012345".toBigDecimal()

    override fun otherValueNotInFixedValues(): BigDecimal = "49.99".toBigDecimal()

    override fun fixedValuesIncludingSample(sample: BigDecimal): Set<BigDecimal> = setOf(
        19.99.toBigDecimal(),
        99.99.toBigDecimal(),
        199.99.toBigDecimal(),
        sample,
    )

    override fun assertJsonHasValue(
        jsonString: String,
        expectedValue: BigDecimal,
    ) {
        assertTrue(
            jsonString.contains(""""value":"$expectedValue""""),
            "JSON missing BigDecimal value: $jsonString",
        )
    }
}
