@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.property

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.core.createOrUpdateProperty
import com.yonatankarp.ff4k.core.getPropertyOrThrow
import com.yonatankarp.ff4k.core.getPropertyValue
import com.yonatankarp.ff4k.core.getPropertyValueOrDefault
import com.yonatankarp.ff4k.exception.PropertyAlreadyExistsException
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreFixture.DEFAULT_VALUE
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreFixture.PROPERTY_NAME
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

internal fun FunSpec.propertyStoreCrudTests(createStore: suspend () -> PropertyStore) {
    test("should create a new property") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)

        // When
        store += property

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        retrieved.shouldNotBeNull()
        retrieved.name shouldBe PROPERTY_NAME
        retrieved.value shouldBe DEFAULT_VALUE
    }

    test("should throw exception when creating duplicate property") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)
        store += property

        // When / Then
        shouldThrow<PropertyAlreadyExistsException> {
            store += property
        }
    }

    test("should create multiple properties with different names") {
        // Given
        val store = createStore()

        // When
        store += PropertyInt(name = "prop1", value = 1)
        store += PropertyInt(name = "prop2", value = 2)
        store += PropertyInt(name = "prop3", value = 3)

        // Then
        store.getAll().size shouldBe 3
        store.get<Int>("prop1")?.value shouldBe 1
        store.get<Int>("prop2")?.value shouldBe 2
        store.get<Int>("prop3")?.value shouldBe 3
    }

    test("should read property by name") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = 42)
        store += property

        // When
        val retrieved = store.get<Int>(PROPERTY_NAME)

        // Then
        retrieved.shouldNotBeNull()
        retrieved.name shouldBe PROPERTY_NAME
        retrieved.value shouldBe 42
    }

    test("should return null when reading non-existent property") {
        // Given
        val store = createStore()

        // When
        val retrieved = store.get<Int>("non-existent")

        // Then
        retrieved.shouldBeNull()
    }

    test("should read all properties") {
        // Given
        val store = createStore()
        store += PropertyInt(name = "prop1", value = 1)
        store += PropertyInt(name = "prop2", value = 2)
        store += PropertyString(name = "prop3", value = "test")

        // When
        val allProperties = store.getAll()

        // Then
        allProperties.size shouldBe 3
        allProperties.keys shouldContain "prop1"
        allProperties.keys shouldContain "prop2"
        allProperties.keys shouldContain "prop3"
    }

    test("should return empty map when no properties exist") {
        // Given
        val store = createStore()

        // When
        val allProperties = store.getAll()

        // Then
        allProperties.shouldBeEmpty()
    }

    test("should get property or default when property exists") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 42)
        val defaultProperty = PropertyInt(name = PROPERTY_NAME, value = 99)

        // When
        val result = store.getOrDefault(PROPERTY_NAME, defaultProperty)

        // Then
        result.value shouldBe 42
    }

    test("should get default when property does not exist") {
        // Given
        val store = createStore()
        val defaultProperty = PropertyInt(name = PROPERTY_NAME, value = 99)

        // When
        val result = store.getOrDefault(PROPERTY_NAME, defaultProperty)

        // Then
        result.value shouldBe 99
    }

    test("should update existing property") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = 10)
        store += property

        // When
        val updated = PropertyInt(name = PROPERTY_NAME, value = 20)
        store.updateProperty(updated)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        retrieved.shouldNotBeNull()
        retrieved.value shouldBe 20
    }

    test("should update property using transform function") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 10)

        // When
        store.updateProperty<Int>(PROPERTY_NAME) { property ->
            PropertyInt(
                name = PROPERTY_NAME,
                value = property.value * 2,
                description = property.description,
            )
        }

        // Then
        val updated = store.get<Int>(PROPERTY_NAME)
        updated.shouldNotBeNull()
        updated.value shouldBe 20
    }

    test("should throw exception when updating non-existent property with transform") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<PropertyNotFoundException> {
            store.updateProperty<Int>(PROPERTY_NAME) { it }
        }
    }

    test("should delete property") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)
        store += property

        // When
        store -= PROPERTY_NAME

        // Then
        store.get<Int>(PROPERTY_NAME).shouldBeNull()
    }

    test("should throw exception when deleting non-existent property") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<PropertyNotFoundException> {
            store -= PROPERTY_NAME
        }
    }

    test("should check if property exists using contains operator") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)
        store += property

        // Then
        (PROPERTY_NAME in store).shouldBeTrue()
        ("non-existent" in store).shouldBeFalse()
    }

    test("should list property names") {
        // Given
        val store = createStore()
        store += PropertyInt(name = "prop1", value = 1)
        store += PropertyInt(name = "prop2", value = 2)
        store += PropertyInt(name = "prop3", value = 3)

        // When
        val names = store.listPropertyIds()

        // Then
        names.size shouldBe 3
        names shouldContain "prop1"
        names shouldContain "prop2"
        names shouldContain "prop3"
    }

    test("should return empty set when no properties exist for listPropertyNames") {
        // Given
        val store = createStore()

        // When
        val names = store.listPropertyIds()

        // Then
        names.shouldBeEmpty()
    }

    test("should clear all properties") {
        // Given
        val store = createStore()
        store += PropertyInt(name = "prop1", value = 1)
        store += PropertyInt(name = "prop2", value = 2)
        store += PropertyInt(name = "prop3", value = 3)

        // When
        store.clear()

        // Then
        store.getAll().shouldBeEmpty()
    }

    test("should check if store is empty using extension function") {
        // Given
        val store = createStore()

        // Then
        store.isEmpty().shouldBeTrue()

        // When
        store += PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)

        // Then
        store.isEmpty().shouldBeFalse()
    }

    test("isEmpty property should return true for empty store") {
        // Given
        val store = createStore()

        // Then
        store.isEmpty().shouldBeTrue()
    }

    test("isEmpty property should return false for non-empty store") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)

        // Then
        store.isEmpty().shouldBeFalse()
    }

    test("should return count 0 when store is empty") {
        // Given
        val store = createStore()

        // When
        val result = store.count()

        // Then
        result shouldBe 0
    }

    test("should count properties in store") {
        // Given
        val store = createStore()
        store += PropertyInt(name = "prop1", value = 1)
        store += PropertyInt(name = "prop2", value = 2)
        store += PropertyInt(name = "prop3", value = 3)

        // When
        val result = store.count()

        // Then
        result shouldBe 3
    }

    test("should create or update property - create path") {
        // Given
        val store = createStore()
        val property = PropertyInt(name = PROPERTY_NAME, value = 42)

        // When
        store.createOrUpdateProperty(property)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        retrieved.shouldNotBeNull()
        retrieved.value shouldBe 42
    }

    test("should create or update property - update path") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 10)

        // When
        val updated = PropertyInt(name = PROPERTY_NAME, value = 42)
        store.createOrUpdateProperty(updated)

        // Then
        val retrieved = store.get<Int>(PROPERTY_NAME)
        retrieved.shouldNotBeNull()
        retrieved.value shouldBe 42
    }

    test("should get property or throw exception") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = DEFAULT_VALUE)

        // When
        val property = store.getPropertyOrThrow<Int>(PROPERTY_NAME)

        // Then
        property.shouldNotBeNull()
        property.name shouldBe PROPERTY_NAME
    }

    test("should throw exception when getting non-existent property with getPropertyOrThrow") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<PropertyNotFoundException> {
            store.getPropertyOrThrow<Int>(PROPERTY_NAME)
        }
    }

    test("should get property value directly") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 42)

        // When
        val value = store.getPropertyValue<Int>(PROPERTY_NAME)

        // Then
        value shouldBe 42
    }

    test("should return null when getting value of non-existent property") {
        // Given
        val store = createStore()

        // When
        val value = store.getPropertyValue<Int>(PROPERTY_NAME)

        // Then
        value.shouldBeNull()
    }

    test("should get property value or default") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 42)

        // When
        val value = store.getPropertyValueOrDefault(PROPERTY_NAME, 99)

        // Then
        value shouldBe 42
    }

    test("should return default when getting value of non-existent property") {
        // Given
        val store = createStore()

        // When
        val value = store.getPropertyValueOrDefault(PROPERTY_NAME, 99)

        // Then
        value shouldBe 99
    }
}
