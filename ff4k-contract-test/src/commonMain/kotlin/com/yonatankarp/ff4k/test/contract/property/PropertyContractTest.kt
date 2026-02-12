@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.property

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import com.yonatankarp.ff4k.property.Property
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

@Ignored
abstract class PropertyContractTest<V, P : Property<V>> : FunSpec() {

    protected open val json = Json {
        serializersModule = humanReadableSerializerModule
    }

    protected abstract val serializer: KSerializer<P>

    protected abstract fun create(
        name: String,
        value: V,
        description: String? = null,
        fixedValues: Set<V> = emptySet(),
        readOnly: Boolean = false,
    ): P

    protected abstract fun sampleName(): String
    protected abstract fun sampleValue(): V
    protected abstract fun otherValueNotInFixedValues(): V
    protected abstract fun fixedValuesIncludingSample(sample: V): Set<V>

    protected open fun assertJsonHasName(
        jsonString: String,
        expectedName: String,
    ) {
        jsonString shouldContain """"name":"$expectedName""""
    }

    protected open fun assertJsonHasValue(
        jsonString: String,
        expectedValue: V,
    ) {
        jsonString shouldContain """"value":$expectedValue"""
    }

    init {
        test("stores name and value") {
            // Given
            val name = sampleName()
            val value = sampleValue()

            // When
            val property = create(name = name, value = value)

            // Then
            property.name shouldBe name
            property.value shouldBe value
        }

        test("serializes to JSON") {
            // Given
            val name = sampleName()
            val value = sampleValue()
            val property = create(name = name, value = value)

            // When
            val jsonString = json.encodeToString(serializer, property)

            // Then
            assertJsonHasName(jsonString, name)
            assertJsonHasValue(jsonString, value)
        }

        test("deserializes from JSON") {
            // Given
            val original = create(
                name = sampleName(),
                value = sampleValue(),
                description = "Some description",
            )

            // When
            val jsonString = json.encodeToString(serializer, original)
            val deserialized = json.decodeFromString(serializer, jsonString)

            // Then
            deserialized shouldBe original
        }

        test("hasFixedValues returns false when no fixed values") {
            // Given
            val name = sampleName()
            val value = sampleValue()

            // When
            val property = create(name = name, value = value)

            // Then
            property.hasFixedValues.shouldBeFalse()
        }

        test("hasFixedValues returns true when fixed values defined") {
            // Given
            val name = sampleName()
            val value = sampleValue()
            val fixed = fixedValuesIncludingSample(value)

            // When
            val property = create(name = name, value = value, fixedValues = fixed)

            // Then
            property.hasFixedValues.shouldBeTrue()
        }

        test("should create property without errors when no fixed values provided") {
            // Given
            val name = sampleName()
            val value = sampleValue()

            // When / Then
            val property = create(name = name, value = value)

            // Then
            property.name shouldBe name
            property.value shouldBe value
        }

        test("should create property without errors when value within fixed values") {
            // Given
            val name = sampleName()
            val value = sampleValue()
            val fixedValues = fixedValuesIncludingSample(value)

            // When / Then
            val property = create(name = name, value = value, fixedValues = fixedValues)

            // Then
            property.name shouldBe name
            property.value shouldBe value
        }

        test("should create property without errors when fixed values is empty") {
            // Given
            val name = sampleName()
            val value = sampleValue()
            val fixedValues = emptySet<V>()

            // When / Then
            val property = create(name = name, value = value, fixedValues = fixedValues)

            // Then
            property.name shouldBe name
            property.value shouldBe value
        }

        test("should throw exception when value is not within fixed values") {
            // Given
            val name = sampleName()
            val value = otherValueNotInFixedValues()
            val fixed = fixedValuesIncludingSample(sampleValue())

            // When / Then
            shouldThrow<IllegalArgumentException> {
                create(name = name, value = value, fixedValues = fixed)
            }
        }
    }
}
