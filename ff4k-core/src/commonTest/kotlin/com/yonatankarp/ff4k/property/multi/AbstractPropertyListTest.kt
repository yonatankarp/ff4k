package com.yonatankarp.ff4k.property.multi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

internal class AbstractPropertyListTest :
    FunSpec({

        test("addAll at index inserts elements at the right position") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "d"),
            )
            val elementsToInsert = listOf("b", "c")

            // When
            val changed = property.addAll(1, elementsToInsert)

            // Then
            changed.shouldBeTrue()
            property shouldBe listOf("a", "b", "c", "d")
        }

        test("get returns element at index") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c"),
            )

            // When
            val element = property[1]

            // Then
            element shouldBe "b"
        }

        test("set updates element at index and returns the element") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c"),
            )

            // When
            val returned = property.set(1, "x")

            // Then
            returned shouldBe "x"
            property shouldBe listOf("a", "x", "c")
        }

        test("add at index inserts element") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "c"),
            )

            // When
            property.add(1, "b")

            // Then
            property shouldBe listOf("a", "b", "c")
        }

        test("removeAt removes and returns element") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c"),
            )

            // When
            val removed = property.removeAt(1)

            // Then
            removed shouldBe "b"
            property shouldBe listOf("a", "c")
        }

        test("indexOf returns first index of element") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "a"),
            )

            // When
            val index = property.indexOf("a")

            // Then
            index shouldBe 0
        }

        test("lastIndexOf returns last index of element") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "a"),
            )

            // When
            val index = property.lastIndexOf("a")

            // Then
            index shouldBe 2
        }

        test("listIterator iterates over current elements in order") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c"),
            )

            // When
            val iterated = buildList {
                val iterator = property.listIterator()
                while (iterator.hasNext()) add(iterator.next())
            }

            // Then
            iterated shouldBe listOf("a", "b", "c")
        }

        test("subList returns correct slice") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c", "d"),
            )

            // When
            val slice = property.subList(1, 3)

            // Then
            slice shouldBe listOf("b", "c")
        }

        test("listIterator with index starts iteration at the given position") {
            // Given
            val property = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b", "c", "d"),
            )
            val startIndex = 2

            // When
            val iterator = property.listIterator(startIndex)
            val iterated = buildList {
                while (iterator.hasNext()) add(iterator.next())
            }

            // Then
            iterated shouldBe listOf("c", "d")
            property.listIterator(0).hasNext().shouldBeTrue()
        }

        test("list properties are equal and have same hashCode when all fields match") {
            // Given
            val propertyName = "list"
            val value = mutableListOf("a", "b")
            val description = "desc"
            val fixedValues1 = mutableSetOf(mutableListOf("a", "b"))
            val fixedValues2 = mutableSetOf(mutableListOf("a", "b"))
            val readOnly = true

            val first = StringListProperty(propertyName, value.toMutableList(), description, fixedValues1, readOnly)
            val second = StringListProperty(propertyName, value.toMutableList(), description, fixedValues2, readOnly)

            // When
            val equals = first == second
            val hashEquals = first.hashCode() == second.hashCode()

            // Then
            equals shouldBe true
            hashEquals shouldBe true
        }

        test("list properties are not equal when any field differs") {
            // Given
            val base = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableListOf("a", "b")),
                readOnly = true,
            )

            val differentName = StringListProperty(
                name = "other",
                value = mutableListOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableListOf("a", "b")),
                readOnly = true,
            )

            val differentValue = StringListProperty(
                name = "list",
                value = mutableListOf("a", "c"),
                description = "desc",
                fixedValues = mutableSetOf(mutableListOf("a", "c")),
                readOnly = true,
            )

            val differentDescription = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b"),
                description = "different",
                fixedValues = mutableSetOf(mutableListOf("a", "b")),
                readOnly = true,
            )

            val differentFixedValues = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableListOf("a", "b"), mutableListOf("a", "c")),
                readOnly = true,
            )

            val differentReadOnly = StringListProperty(
                name = "list",
                value = mutableListOf("a", "b"),
                description = "desc",
                fixedValues = mutableSetOf(mutableListOf("a", "b")),
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
    private class StringListProperty(
        name: String,
        value: MutableList<String> = mutableListOf(),
        description: String? = null,
        fixedValues: MutableSet<MutableList<String>> = mutableSetOf(),
        readOnly: Boolean = false,
    ) : AbstractPropertyList<String>(name, value, description, fixedValues, readOnly)
}
