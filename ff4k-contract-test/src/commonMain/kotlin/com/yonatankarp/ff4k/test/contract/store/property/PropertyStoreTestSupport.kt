package com.yonatankarp.ff4k.test.contract.store.property

import com.yonatankarp.ff4k.core.PropertyStore

/**
 * Support interface for PropertyStore tests.
 */
internal interface PropertyStoreTestSupport {
    /**
     * Create a fresh, empty PropertyStore instance for each test.
     */
    suspend fun createStore(): PropertyStore

    companion object {
        const val PROPERTY_NAME = "testProperty"
        const val DEFAULT_VALUE = 42
    }
}
