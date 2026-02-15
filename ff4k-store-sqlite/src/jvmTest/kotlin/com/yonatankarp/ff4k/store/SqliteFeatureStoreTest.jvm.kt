package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.test.contract.store.FeatureStoreContractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain

class JvmSqliteFeatureStoreTest :
    FeatureStoreContractTest({

        // Note: Input validation for plusAssign and createOrUpdate cannot be tested here
        // because the Feature constructor validates uid before the store sees it.
        // Only minusAssign accepts a raw featureId string that can be validated.
        context("input validation edge cases") {
            test("minusAssign should throw IllegalArgumentException when featureId is empty") {
                // Given
                val store = SqliteFeatureStore(createSqliteDriver())

                // When / Then
                val exception = shouldThrow<IllegalArgumentException> {
                    store -= ""
                }
                exception.message shouldContain "featureId cannot be empty"
            }

            test("minusAssign should throw IllegalArgumentException when featureId is blank") {
                // Given
                val store = SqliteFeatureStore(createSqliteDriver())

                // When / Then
                val exception = shouldThrow<IllegalArgumentException> {
                    store -= "   "
                }
                exception.message shouldContain "featureId cannot be empty"
            }
        }
    }) {
    override suspend fun createStore(): FeatureStore = SqliteFeatureStore(createSqliteDriver())
}
