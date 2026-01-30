package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.config.FF4kConfiguration
import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.property.utils.property
import com.yonatankarp.ff4k.test.contract.store.PropertyStoreContractTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

internal class InMemoryPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = InMemoryPropertyStore()

    init {
        test("test store initialized with property map contains properties") {
            // Given
            val initialProperties = mapOf(
                "prop-1" to property("prop-1", "value1"),
                "prop-2" to property("prop-2", 42),
                "prop-3" to property("prop-3", true),
            )

            // When
            val store = InMemoryPropertyStore(initialProperties)

            // Then
            store.getAll().size shouldBe 3
            store.contains("prop-1").shouldBeTrue()
            store.contains("prop-2").shouldBeTrue()
            store.contains("prop-3").shouldBeTrue()
            store.get<String>("prop-1").shouldNotBeNull().let { it.value shouldBe "value1" }
            store.get<Int>("prop-2").shouldNotBeNull().let { it.value shouldBe 42 }
            store.get<Boolean>("prop-3").shouldNotBeNull().let { it.value shouldBe true }
        }

        test("test store initialized with FF4kConfiguration contains properties") {
            // Given
            val config = FF4kConfiguration(
                properties = mapOf(
                    "config-prop-1" to property("config-prop-1", "configValue"),
                    "config-prop-2" to property("config-prop-2", 100),
                ),
            )

            // When
            val store = InMemoryPropertyStore(config)

            // Then
            store.getAll().size shouldBe 2
            store.contains("config-prop-1").shouldBeTrue()
            store.contains("config-prop-2").shouldBeTrue()
            store.get<String>("config-prop-1").shouldNotBeNull().let { it.value shouldBe "configValue" }
            store.get<Int>("config-prop-2").shouldNotBeNull().let { it.value shouldBe 100 }
        }

        test("test store initialized with empty configuration is empty") {
            // Given
            val config = FF4kConfiguration()

            // When
            val store = InMemoryPropertyStore(config)

            // Then
            store.isEmpty().shouldBeTrue()
            store.getAll().size shouldBe 0
        }
    }
}
