package com.yonatankarp.ff4k.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

internal class FeaturesGroupTest :
    FunSpec({

        test("addGroup should return new feature with group assigned") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.addGroup(GROUP_NAME)

            // Then
            updated.group shouldBe GROUP_NAME
            feature.group.shouldBeNull() // Original unchanged
        }

        test("addGroup should overwrite existing group") {
            // Given
            val feature = Feature(uid = FEATURE_UID, group = EXISTING_GROUP)

            // When
            val updated = feature.addGroup(GROUP_NAME)

            // Then
            updated.group shouldBe GROUP_NAME
        }

        test("removeGroup should return new feature with no group") {
            // Given
            val feature = Feature(uid = FEATURE_UID, group = GROUP_NAME)

            // When
            val updated = feature.removeGroup()

            // Then
            updated.group.shouldBeNull()
            feature.group shouldBe GROUP_NAME // Original unchanged
        }

        test("removeGroup should work on feature with no group") {
            // Given
            val feature = Feature(uid = FEATURE_UID)

            // When
            val updated = feature.removeGroup()

            // Then
            updated.group.shouldBeNull()
        }
    }) {
    private companion object {
        private const val FEATURE_UID = "my-feature"
        private const val GROUP_NAME = "beta-users"
        private const val EXISTING_GROUP = "alpha-users"
    }
}
