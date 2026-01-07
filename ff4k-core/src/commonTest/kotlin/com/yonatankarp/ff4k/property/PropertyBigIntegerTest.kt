package com.yonatankarp.ff4k.property

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import kotlinx.serialization.json.Json
import kotlin.test.assertTrue

/**
 * Tests for PropertyBigInteger class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBigIntegerTest : PropertyContractTest<BigInteger, PropertyBigInteger>() {

    override val json: Json = Json {
        serializersModule = humanReadableSerializerModule
    }

    override val serializer = PropertyBigInteger.serializer()

    override fun create(
        name: String,
        value: BigInteger,
        description: String?,
        fixedValues: Set<BigInteger>,
        readOnly: Boolean
    ): PropertyBigInteger = PropertyBigInteger(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly
    )

    override fun sampleName(): String = "largeNumber"
    override fun sampleValue(): BigInteger = "12345678901234567890".toBigInteger()

    override fun otherValueNotInFixedValues(): BigInteger = "5000".toBigInteger()

    override fun fixedValuesIncludingSample(sample: BigInteger): Set<BigInteger> =
        setOf(
            100.toBigInteger(),
            1_000.toBigInteger(),
            10_000.toBigInteger(),
            sample)

    override fun assertJsonHasValue(jsonString: String, expectedValue: BigInteger) {
        assertTrue(
            jsonString.contains(""""value":"$expectedValue""""),
            "JSON missing BigInteger value: $jsonString"
        )
    }
}
