package com.yonatankarp.ff4k

interface PropertyStore {
    suspend fun get(name: String): Property<Any>?

    /** Returns every property, leaving out stored documents that cannot be decoded. */
    suspend fun getAll(): List<Property<Any>>

    /** Inserts or replaces the property. */
    suspend fun put(property: Property<Any>)

    /** Removes the property; a no-op when it does not exist. */
    suspend fun delete(name: String)
}
