package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

internal fun FunSpec.sqliteUniqueConstraintTests(createStore: suspend () -> SqliteFeatureStore) {
    context("unique constraint violation detection") {
        test("should throw FeatureAlreadyExistsException when inserting duplicate feature") {
            val store = createStore()
            val feature = Feature(uid = "duplicate-test", isEnabled = true)
            store += feature

            val exception = shouldThrow<FeatureAlreadyExistsException> {
                store += feature
            }

            exception.message shouldBe "Feature already exists: duplicate-test"
        }

        test("should preserve original exception as cause") {
            val store = createStore()
            val feature = Feature(uid = "cause-test", isEnabled = true)
            store += feature

            val exception = shouldThrow<FeatureAlreadyExistsException> {
                store += feature
            }

            exception.cause.shouldNotBeNull()
        }

        test("should detect constraint violation with different feature content but same uid") {
            val store = createStore()
            val feature1 = Feature(
                uid = "same-uid",
                isEnabled = true,
                description = "First version",
            )
            val feature2 = Feature(
                uid = "same-uid",
                isEnabled = false,
                description = "Second version",
            )

            store += feature1

            shouldThrow<FeatureAlreadyExistsException> {
                store += feature2
            }
        }

        test("should allow inserting features with different uids") {
            val store = createStore()
            val feature1 = Feature(uid = "feature-1", isEnabled = true)
            val feature2 = Feature(uid = "feature-2", isEnabled = true)

            store += feature1
            store += feature2

            store.count() shouldBe 2
        }
    }
}
