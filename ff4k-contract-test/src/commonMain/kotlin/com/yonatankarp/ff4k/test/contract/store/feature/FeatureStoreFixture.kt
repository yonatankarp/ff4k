package com.yonatankarp.ff4k.test.contract.store.feature

import com.yonatankarp.ff4k.core.Feature

internal object FeatureStoreFixture {
    const val FEATURE_NAME = "feature"
    const val GROUP_NAME = "group"
    const val ANOTHER_GROUP_NAME = "anotherGroup"
    const val ROLE = "admin"

    fun createFeature(
        uid: String = FEATURE_NAME,
        isEnabled: Boolean = false,
    ) = Feature(uid = uid, isEnabled = isEnabled)
}
