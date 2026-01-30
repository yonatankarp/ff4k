@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.exception.GroupNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.ANOTHER_GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.GROUP_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.createFeature
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

internal fun FunSpec.featureStoreGroupTests(createStore: suspend () -> FeatureStore) {
    test("should enable all features in a group") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = false)
        store += createFeature(uid = "feature2", isEnabled = false)
        store += createFeature(uid = "feature3", isEnabled = false)
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)

        // When
        store.enableGroup(GROUP_NAME)

        // Then
        store["feature1"].shouldNotBeNull().isEnabled.shouldBeTrue()
        store["feature2"].shouldNotBeNull().isEnabled.shouldBeTrue()
        store["feature3"].shouldNotBeNull().isEnabled.shouldBeFalse()
    }

    test("should disable all features in a group") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1", isEnabled = true)
        store += createFeature(uid = "feature2", isEnabled = true)
        store += createFeature(uid = "feature3", isEnabled = true)
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)

        // When
        store.disableGroup(GROUP_NAME)

        // Then
        store["feature1"].shouldNotBeNull().isEnabled.shouldBeFalse()
        store["feature2"].shouldNotBeNull().isEnabled.shouldBeFalse()
        store["feature3"].shouldNotBeNull().isEnabled.shouldBeTrue()
    }

    test("should add feature to group") {
        // Given
        val store = createStore()
        val feature = createFeature()
        store += feature

        // When
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        groupFeatures.size shouldBe 1
        groupFeatures shouldContainKey FEATURE_NAME
    }

    test("should throw exception when adding non-existent feature to group") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.addToGroup(FEATURE_NAME, GROUP_NAME)
        }
    }

    test("should remove feature from group") {
        // Given
        val store = createStore()
        store += createFeature(uid = FEATURE_NAME)
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.removeFromGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        groupFeatures.shouldBeEmpty()
    }

    test("should throw exception when removing non-existent feature from group") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.removeFromGroup(FEATURE_NAME, GROUP_NAME)
        }
    }

    test("should throw exception when removing group the feature is not in") {
        // Given
        val store = createStore()
        store += feature(uid = FEATURE_NAME) {
            group = GROUP_NAME
        }

        // When / Then
        shouldThrow<GroupNotFoundException> {
            store.removeFromGroup(FEATURE_NAME, ANOTHER_GROUP_NAME)
        }
    }

    test("should get all features in a group") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)
        store.addToGroup("feature3", ANOTHER_GROUP_NAME)

        // When
        val group1Features = store.getGroup(GROUP_NAME)

        // Then
        group1Features.size shouldBe 2
        group1Features shouldContainKey "feature1"
        group1Features shouldContainKey "feature2"
        group1Features shouldNotContainKey "feature3"
    }

    test("should return empty map for non-existent group") {
        // Given
        val store = createStore()

        // When
        val groupFeatures = store.getGroup(GROUP_NAME)

        // Then
        groupFeatures.shouldBeEmpty()
    }

    test("should check if group exists") {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        store.containsGroup(GROUP_NAME).shouldBeTrue()
        store.containsGroup(ANOTHER_GROUP_NAME).shouldBeFalse()
    }

    test("should get all group names") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store += createFeature(uid = "feature3")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", GROUP_NAME)
        store.addToGroup("feature3", ANOTHER_GROUP_NAME)

        // When
        val groups = store.getAllGroups()

        // Then
        groups.size shouldBe 2
        groups shouldContain GROUP_NAME
        groups shouldContain ANOTHER_GROUP_NAME
    }

    test("should return empty set when no groups exist") {
        // Given
        val store = createStore()

        // When
        val groups = store.getAllGroups()

        // Then
        groups.shouldBeEmpty()
    }

    test("should allow adding feature to same group multiple times without error") {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        val groupFeatures = store.getGroup(GROUP_NAME)
        groupFeatures.size shouldBe 1
        groupFeatures shouldContainKey FEATURE_NAME
    }

    test("should move feature from one group to another") {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.addToGroup(FEATURE_NAME, ANOTHER_GROUP_NAME)

        // Then
        val newGroupFeatures = store.getGroup(ANOTHER_GROUP_NAME)
        newGroupFeatures.size shouldBe 1
        newGroupFeatures shouldContainKey FEATURE_NAME

        val oldGroupFeatures = store.getGroup(GROUP_NAME)
        oldGroupFeatures.shouldBeEmpty()
    }

    test("should clean up empty group after removing last feature") {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store.removeFromGroup(FEATURE_NAME, GROUP_NAME)

        // Then
        store.containsGroup(GROUP_NAME).shouldBeFalse()
        val groups = store.getAllGroups()
        groups shouldNotContain GROUP_NAME
    }

    test("should clean up group when deleting last feature in group") {
        // Given
        val store = createStore()
        store += createFeature()
        store.addToGroup(FEATURE_NAME, GROUP_NAME)

        // When
        store -= FEATURE_NAME

        // Then
        store.containsGroup(GROUP_NAME).shouldBeFalse()
        val groups = store.getAllGroups()
        groups shouldNotContain GROUP_NAME
    }

    test("should clear groups along with features") {
        // Given
        val store = createStore()
        store += createFeature(uid = "feature1")
        store += createFeature(uid = "feature2")
        store.addToGroup("feature1", GROUP_NAME)
        store.addToGroup("feature2", ANOTHER_GROUP_NAME)

        // When
        store.clear()

        // Then
        val groups = store.getAllGroups()
        groups.shouldBeEmpty()
        store.containsGroup(GROUP_NAME).shouldBeFalse()
        store.containsGroup(ANOTHER_GROUP_NAME).shouldBeFalse()
    }
}
