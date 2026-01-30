package com.yonatankarp.ff4k.property.multi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal class AbstractPropertySetTest :
    FunSpec({

        test("defaults are description null fixedValues empty and readOnly false") {
            // Given
            val property = StringSetProperty(name = "set")

            // When
            val description = property.description
            val fixedValues = property.fixedValues
            val readOnly = property.readOnly

            // Then
            description.shouldBeNull()
            fixedValues.shouldBeEmpty()
            readOnly.shouldBeFalse()
        }

        test("add inserts element and increases size") {
            // Given
            val property = StringSetProperty(name = "set")

            // When
            val changed = property.add("a")

            // Then
            changed.shouldBeTrue()
            property.size shouldBe 1
            property.contains("a").shouldBeTrue()
        }

        test("add does not add duplicates") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a"))

            // When
            val firstAddChanged = property.add("a")
            val sizeAfterAdd = property.size

            // Then
            firstAddChanged.shouldBeFalse()
            sizeAfterAdd shouldBe 1
        }

        test("plusAssign adds element") {
            // Given
            val property = StringSetProperty(name = "set")

            // When
            property += "a"

            // Then
            property.contains("a").shouldBeTrue()
            property.size shouldBe 1
        }

        test("remove removes existing element") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

            // When
            val changed = property.remove("a")

            // Then
            changed.shouldBeTrue()
            property.contains("a").shouldBeFalse()
            property.value shouldBe setOf("b")
        }

        test("minusAssign removes element") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

            // When
            property -= "b"

            // Then
            property.contains("b").shouldBeFalse()
            property.value shouldBe setOf("a")
        }

        test("addAll adds all new elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a"))
            val elements = listOf("b", "c")

            // When
            val changed = property.addAll(elements)

            // Then
            changed.shouldBeTrue()
            property.value shouldBe setOf("a", "b", "c")
        }

        test("addAll vararg adds all new elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a"))

            // When
            property.addAll("b", "c")

            // Then
            property.value shouldBe setOf("a", "b", "c")
        }

        test("containsAll returns true when all elements present") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
            val required = listOf("a", "c")

            // When
            val result = property.containsAll(required)

            // Then
            result.shouldBeTrue()
        }

        test("removeAll removes provided elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
            val elementsToRemove = listOf("b", "c")

            // When
            val changed = property.removeAll(elementsToRemove)

            // Then
            changed.shouldBeTrue()
            property.value shouldBe setOf("a")
        }

        test("retainAll keeps only provided elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))
            val elementsToKeep = listOf("a", "c")

            // When
            val changed = property.retainAll(elementsToKeep)

            // Then
            changed.shouldBeTrue()
            property.value shouldBe setOf("a", "c")
        }

        test("iterator iterates over all elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b", "c"))

            // When
            val iterated = mutableSetOf<String>()
            val iterator = property.iterator()
            while (iterator.hasNext()) {
                iterated.add(iterator.next())
            }

            // Then
            iterated shouldBe setOf("a", "b", "c")
        }

        test("clear removes all elements") {
            // Given
            val property = StringSetProperty(name = "set", value = mutableSetOf("a", "b"))

            // When
            property.clear()

            // Then
            property.isEmpty().shouldBeTrue()
            property.size shouldBe 0
            property.value.shouldBeEmpty()
        }

        test("set properties are equal and have same hashCode when all fields match") {
            // Given
            val propertyName = "set"
            val value = mutableSetOf("a", "b")
            val description = "desc"
            val fixedValues = mutableSetOf(mutableSetOf("a", "b"))
            val readOnly = true

            val first = StringSetProperty(propertyName, value.toMutableSet(), description, fixedValues, readOnly)
            val second = StringSetProperty(propertyName, value.toMutableSet(), description, fixedValues, readOnly)

            // When
            val equals = first == second
            val hashEquals = first.hashCode() == second.hashCode()

            // Then
            equals shouldBe true
            hashEquals shouldBe true
        }

        test("set properties are not equal when any field differs") {
            // Given
            val base = StringSetProperty(
                name = "set",
                value = mutableSetOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableSetOf("a", "b"), mutableSetOf("a", "c")),
                readOnly = true,
            )

            val differentName = StringSetProperty(
                name = "other",
                value = mutableSetOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableSetOf("a", "b"), mutableSetOf("a", "c")),
                readOnly = true,
            )

            val differentValue = StringSetProperty(
                name = "set",
                value = mutableSetOf("a", "c"),
                description = "desc",
                fixedValues = mutableSetOf(mutableSetOf("a", "b"), mutableSetOf("a", "c")),
                readOnly = true,
            )

            val differentDescription = StringSetProperty(
                name = "set",
                value = mutableSetOf("a", "b"),
                description = "different",
                fixedValues = mutableSetOf(mutableSetOf("a", "b"), mutableSetOf("a", "c")),
                readOnly = true,
            )

            val differentFixedValues = StringSetProperty(
                name = "set",
                value = mutableSetOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableSetOf("a", "b")),
                readOnly = true,
            )

            val differentReadOnly = StringSetProperty(
                name = "set",
                value = mutableSetOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableSetOf("a", "b"), mutableSetOf("a", "c")),
                readOnly = false,
            )

            // When / Then
            (base == differentName).shouldBeFalse()
            (base == differentValue).shouldBeFalse()
            (base == differentDescription).shouldBeFalse()
            (base == differentFixedValues).shouldBeFalse()
            (base == differentReadOnly).shouldBeFalse()
        }
    }) {
    private class StringSetProperty(
        name: String,
        value: MutableSet<String> = mutableSetOf(),
        description: String? = null,
        fixedValues: MutableSet<MutableSet<String>> = mutableSetOf(),
        readOnly: Boolean = false,
    ) : AbstractPropertySet<String, MutableSet<String>>(
        name = name,
        value = value,
        description = description,
        fixedValues = fixedValues,
        readOnly = readOnly,
    )
}
