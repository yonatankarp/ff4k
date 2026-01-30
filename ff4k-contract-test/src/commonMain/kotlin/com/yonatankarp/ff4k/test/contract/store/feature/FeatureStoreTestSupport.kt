package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore

/**
 * Support interface for FeatureStore tests.
 */
internal interface FeatureStoreTestSupport {
    /**
     * Create a fresh, empty FeatureStore instance for each test.
     */
    suspend fun createStore(): FeatureStore

    fun createFeature(
        uid: String = FEATURE_NAME,
        isEnabled: Boolean = false,
    ) = Feature(uid = uid, isEnabled = isEnabled)

    companion object {
        const val FEATURE_NAME = "feature"
        const val GROUP_NAME = "group"
        const val ANOTHER_GROUP_NAME = "anotherGroup"
        const val ROLE = "admin"
    }
}
