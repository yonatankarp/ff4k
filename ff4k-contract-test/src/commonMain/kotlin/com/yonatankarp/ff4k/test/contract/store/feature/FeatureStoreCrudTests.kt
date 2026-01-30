@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.count
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.createFeature
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

internal fun FunSpec.featureStoreCrudTests(createStore: suspend () -> FeatureStore) {
    test("should create a new feature") {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)

        // When
        store += feature

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.uid shouldBe FEATURE_NAME
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should throw exception when creating duplicate feature") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When / Then
        shouldThrow<FeatureAlreadyExistsException> {
            store += feature
        }
    }

    test("should read feature by id") {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)
        store += feature

        // When
        val retrieved = store[FEATURE_NAME]

        // Then
        retrieved.shouldNotBeNull()
        retrieved.uid shouldBe FEATURE_NAME
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should return null when reading non-existent feature") {
        // Given
        val store = createStore()

        // When
        val retrieved = store["non-existent"]

        // Then
        retrieved.shouldBeNull()
    }

    test("should read all features") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = true)
        store += createFeature(uid = "feature2", isEnabled = false)
        store += createFeature(uid = "feature3", isEnabled = true)

        // When
        val allFeatures = store.getAll()

        // Then
        allFeatures.size shouldBe 3
        allFeatures.keys shouldContain "feature1"
        allFeatures.keys shouldContain "feature2"
        allFeatures.keys shouldContain "feature3"
    }

    test("should return empty map when no features exist") {
        // Given
        val store = createStore()

        // When
        val allFeatures = store.getAll()

        // Then
        allFeatures.shouldBeEmpty()
    }

    test("should update existing feature") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        val updated = feature.copy(isEnabled = true)
        store.update(updated)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should throw exception when updating non-existent feature") {
        // Given
        val store = createStore()
        val feature = createFeature()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.update(feature)
        }
    }

    test("should delete feature") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store -= FEATURE_NAME

        // Then
        store[FEATURE_NAME].shouldBeNull()
    }

    test("should throw exception when deleting non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store -= FEATURE_NAME
        }
    }

    test("should check if feature exists using contains operator") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // Then
        (FEATURE_NAME in store).shouldBeTrue()
        ("non-existent" in store).shouldBeFalse()
    }

    test("should enable a disabled feature") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store.enable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should disable an enabled feature") {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)
        store += feature

        // When
        store.disable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeFalse()
    }

    test("should throw exception when enabling non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.enable(FEATURE_NAME)
        }
    }

    test("should throw exception when disabling non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.disable(FEATURE_NAME)
        }
    }

    test("should allow enabling an already enabled feature without error") {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        store.enable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should allow disabling an already disabled feature without error") {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = false)

        // When
        store.disable(FEATURE_NAME)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeFalse()
    }

    test("should clear all features") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")

        // When
        store.clear()

        // Then
        store.getAll().shouldBeEmpty()
    }

    test("should check if store is empty") {
        // Given
        val store = createStore()

        // Then
        store.isEmpty().shouldBeTrue()

        // When
        store += createFeature()

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

    test("should count features in store") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")

        // When
        val result = store.count()

        // Then
        result shouldBe 3
    }

    test("should update feature using transform function") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.updateFeature(FEATURE_NAME) { feature ->
            feature.copy(isEnabled = true)
        }

        // Then
        val updated = store[FEATURE_NAME]
        updated.shouldNotBeNull()
        updated.isEnabled.shouldBeTrue()
    }

    test("should throw exception when updating non-existent feature with transform") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.updateFeature(FEATURE_NAME) { it }
        }
    }

    test("should throw exception when transform changes feature uid") {
        // Given
        val store = createStore()
        store += createFeature()

        // When / Then
        shouldThrow<IllegalStateException> {
            store.updateFeature(FEATURE_NAME) { feature ->
                feature.copy(uid = "different-id")
            }
        }

        // Verify
        store[FEATURE_NAME].shouldNotBeNull()
        store["different-id"].shouldBeNull()
    }

    test("should create or update feature - create path") {
        // Given
        val store = createStore()
        val feature = createFeature(isEnabled = true)

        // When
        store.createOrUpdate(feature)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should create or update feature - update path") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val updated = createFeature(isEnabled = true)
        store.createOrUpdate(updated)

        // Then
        val retrieved = store[FEATURE_NAME]
        retrieved.shouldNotBeNull()
        retrieved.isEnabled.shouldBeTrue()
    }

    test("should toggle feature from disabled to enabled") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.toggle(FEATURE_NAME)

        // Then
        val toggled = store[FEATURE_NAME]
        toggled.shouldNotBeNull()
        toggled.isEnabled.shouldBeTrue()
    }

    test("should toggle feature from enabled to disabled") {
        // Given
        val store = createStore()
        store += createFeature(isEnabled = true)

        // When
        store.toggle(FEATURE_NAME)

        // Then
        val toggled = store[FEATURE_NAME]
        toggled.shouldNotBeNull()
        toggled.isEnabled.shouldBeFalse()
    }

    test("should throw exception when toggling non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.toggle(FEATURE_NAME)
        }
    }

    test("should get feature or throw exception") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        val feature = store.getOrThrow(FEATURE_NAME)

        // Then
        feature.shouldNotBeNull()
        feature.uid shouldBe FEATURE_NAME
    }

    test("should throw exception when getting non-existent feature with getOrThrow") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.getOrThrow(FEATURE_NAME)
        }
    }
}
