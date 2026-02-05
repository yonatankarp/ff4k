@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.property

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.core.createOrUpdateProperty
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.test.contract.store.property.PropertyStoreFixture.PROPERTY_NAME
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

internal fun FunSpec.propertyStoreConcurrencyTests(createStore: suspend () -> PropertyStore) {
    test("concurrent property updates via atomic method should be atomic") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 0)

        // When
        coroutineScope {
            val jobs = (1..100).map {
                launch {
                    store.updateProperty<Int>(PROPERTY_NAME) { property ->
                        PropertyInt(
                            name = PROPERTY_NAME,
                            value = property.value + 1,
                        )
                    }
                }
            }
            jobs.joinAll()
        }

        // Then
        val finalValue = store.get<Int>(PROPERTY_NAME)?.value
        finalValue.shouldNotBeNull()
        finalValue shouldBe 100
    }

    test("concurrent createOrUpdateProperty calls should handle race conditions") {
        // Given
        val store = createStore()

        // When
        coroutineScope {
            val jobs = (1..100).map { i ->
                launch {
                    store.createOrUpdateProperty(
                        PropertyInt(
                            name = PROPERTY_NAME,
                            value = i,
                        ),
                    )
                }
            }
            jobs.joinAll()
        }

        // Then
        val property = store.get<Int>(PROPERTY_NAME)
        property.shouldNotBeNull()
        property.value shouldBeIn 1..100
    }

    test("concurrent property deletions should handle missing properties gracefully") {
        // Given
        val store = createStore()
        (1..10).forEach { i ->
            store += PropertyInt(name = "prop-$i", value = i)
        }

        // When
        coroutineScope {
            val jobs = (1..10).map { i ->
                launch {
                    store -= "prop-$i"
                }
            }
            jobs.joinAll()
        }

        // Then
        store.count() shouldBe 0
    }

    test("concurrent set operations should handle updates correctly") {
        // Given
        val store = createStore()
        store += PropertyInt(name = PROPERTY_NAME, value = 0)

        // When
        coroutineScope {
            val jobs = (1..50).map { i ->
                launch {
                    store.updateProperty(PROPERTY_NAME) { property ->
                        PropertyInt(name = property.name, value = i)
                    }
                }
            }
            jobs.joinAll()
        }

        // Then
        val finalValue = store.get<Int>(PROPERTY_NAME)?.value
        finalValue.shouldNotBeNull()
        finalValue shouldBeIn 1..50
    }
}
