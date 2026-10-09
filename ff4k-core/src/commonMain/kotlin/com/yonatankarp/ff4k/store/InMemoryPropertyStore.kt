package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.Property
import com.yonatankarp.ff4k.PropertyStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryPropertyStore(
    initial: List<Property<Any>> = emptyList(),
) : PropertyStore {
    private val properties = initial.associateBy { it.name }.toMutableMap()
    private val mutex = Mutex()

    override suspend fun get(name: String): Property<Any>? = mutex.withLock { properties[name] }

    override suspend fun getAll(): List<Property<Any>> = mutex.withLock { properties.values.toList() }

    override suspend fun put(property: Property<Any>) {
        mutex.withLock { properties[property.name] = property }
    }

    override suspend fun delete(name: String) {
        mutex.withLock { properties.remove(name) }
    }
}
