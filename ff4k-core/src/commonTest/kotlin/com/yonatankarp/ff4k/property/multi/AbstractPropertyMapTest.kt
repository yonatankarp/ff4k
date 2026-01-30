package com.yonatankarp.ff4k.property.multi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal class AbstractPropertyMapTest :
    FunSpec({

        test("defaults are description null fixedValues empty and readOnly false") {
            // Given
            val property = StringMapProperty(name = "map")

            // When
            val description = property.description
            val fixedValues = property.fixedValues
            val readOnly = property.readOnly

            // Then
            description.shouldBeNull()
            fixedValues.shouldBeEmpty()
            readOnly.shouldBeFalse()
        }

        test("put inserts entry and returns previous value") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v1"))

            // When
            val previous = property.put("A", "v2")
            val current = property["A"]

            // Then
            previous shouldBe "v1"
            current shouldBe "v2"
            property.size shouldBe 1
        }

        test("putAll inserts all entries") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))
            val additional = mapOf("B" to "w", "C" to "x")

            // When
            property.putAll(additional)

            // Then
            property.size shouldBe 3
            property.keys shouldBe setOf("A", "B", "C")
            property["B"] shouldBe "w"
            property["C"] shouldBe "x"
        }

        test("entries exposes backing map entries") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            val entries = property.entries.associate { it.key to it.value }

            // Then
            entries shouldBe mapOf("A" to "v", "B" to "w")
        }

        test("values exposes backing map values") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            val values = property.values.toSet()

            // Then
            values shouldBe setOf("v", "w")
        }

        test("size reflects backing map size") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            val size = property.size

            // Then
            size shouldBe 2
        }

        test("isEmpty reflects backing map") {
            // Given
            val emptyProperty = StringMapProperty(name = "map", value = mutableMapOf())
            val nonEmptyProperty = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

            // When
            val empty = emptyProperty.isEmpty()
            val nonEmpty = nonEmptyProperty.isEmpty()

            // Then
            empty.shouldBeTrue()
            nonEmpty.shouldBeFalse()
        }

        test("containsKey returns true when key exists") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

            // When
            val containsA = property.containsKey("A")
            val containsB = property.containsKey("B")

            // Then
            containsA.shouldBeTrue()
            containsB.shouldBeFalse()
        }

        test("containsValue returns true when value exists") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            val containsV = property.containsValue("v")
            val containsMissing = property.containsValue("missing")

            // Then
            containsV.shouldBeTrue()
            containsMissing.shouldBeFalse()
        }

        test("get returns value for existing key and null for missing key") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v"))

            // When
            val valueForA = property["A"]
            val valueForMissing = property["missing"]

            // Then
            valueForA shouldBe "v"
            valueForMissing.shouldBeNull()
        }

        test("remove removes entry and returns removed value") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            val removed = property.remove("A")

            // Then
            removed shouldBe "v"
            property.containsKey("A").shouldBeFalse()
            property.size shouldBe 1
        }

        test("minusAssign removes entry by key") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            property -= "B"

            // Then
            property.containsKey("B").shouldBeFalse()
            property.size shouldBe 1
            property.keys shouldBe setOf("A")
        }

        test("clear removes all entries") {
            // Given
            val property = StringMapProperty(name = "map", value = mutableMapOf("A" to "v", "B" to "w"))

            // When
            property.clear()

            // Then
            property.isEmpty().shouldBeTrue()
            property.size shouldBe 0
            property.keys.shouldBeEmpty()
        }
    }) {
    private class StringMapProperty(
        name: String,
        value: MutableMap<String, String> = mutableMapOf(),
        description: String? = null,
        fixedValues: Set<MutableMap<String, String>> = emptySet(),
        readOnly: Boolean = false,
    ) : AbstractPropertyMap<String, MutableMap<String, String>>(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )
}
