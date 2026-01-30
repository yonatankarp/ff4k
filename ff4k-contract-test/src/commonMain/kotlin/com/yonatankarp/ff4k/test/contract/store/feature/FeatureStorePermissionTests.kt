@file:Suppress("FunctionName")

package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.FEATURE_NAME
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.ROLE
import com.yonatankarp.ff4k.test.contract.store.feature.FeatureStoreFixture.createFeature
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull

internal fun FunSpec.featureStorePermissionTests(createStore: suspend () -> FeatureStore) {
    test("should grant role to feature") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.permissions shouldContain ROLE
    }

    test("should throw exception when granting role to non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.grantRoleToFeature(FEATURE_NAME, ROLE)
        }
    }

    test("should revoke role from feature") {
        // Given
        val store = createStore()
        store += createFeature()
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // When
        store.revokeRoleFromFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.permissions shouldNotContain ROLE
    }

    test("should throw exception when revoking role from non-existent feature") {
        // Given
        val store = createStore()

        // When / Then
        shouldThrow<FeatureNotFoundException> {
            store.revokeRoleFromFeature(FEATURE_NAME, ROLE)
        }
    }

    test("should allow granting same role multiple times without error") {
        // Given
        val store = createStore()
        store += createFeature()
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // When
        store.grantRoleToFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
        feature.permissions shouldContain ROLE
    }

    test("should allow revoking non-existent role without error") {
        // Given
        val store = createStore()
        store += createFeature()

        // When
        store.revokeRoleFromFeature(FEATURE_NAME, ROLE)

        // Then
        val feature = store[FEATURE_NAME]
        feature.shouldNotBeNull()
    }
}
