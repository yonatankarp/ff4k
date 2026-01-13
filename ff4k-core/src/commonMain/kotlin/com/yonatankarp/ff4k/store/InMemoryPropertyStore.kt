package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.exception.PropertyAlreadyExistsException
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.utils.withReentrantLock
import kotlinx.coroutines.sync.Mutex

/**
 * In-memory implementation of [PropertyStore].
 *
 * This store keeps feature properties in a mutable map backed by process memory.
 * It is intended for tests, local development, or lightweight runtime configurations
 * where persistence across application restarts is not required.
 *
 * ### Concurrency and thread safety
 * All operations on the underlying map are guarded by a [Mutex] and executed via
 * the [withReentrantLock] utility, which provides coroutine-friendly, reentrant
 * mutual exclusion. This ensures that:
 *
 * * concurrent readers and writers are serialized, preventing data races; and
 * * a coroutine that already holds the lock can safely call other methods on this
 *   store without deadlocking.
 *
 * Despite being thread-safe within a single process, this implementation does not
 * provide any cross-process or distributed consistency guarantees.
 *
 * @author Yonatan Karp
 */
class InMemoryPropertyStore(
    initialProperties: Map<String, Property<*>> = emptyMap(),
) : PropertyStore {

    private val properties: MutableMap<String, Property<*>> = initialProperties.toMutableMap()
    private val mutex: Mutex = Mutex()

    override suspend fun isEmpty(): Boolean = mutex.withReentrantLock {
        properties.isEmpty()
    }

    override suspend fun contains(propertyId: String): Boolean = mutex.withReentrantLock {
        propertyId in properties
    }

    override suspend fun <T> plusAssign(property: Property<T>) = mutex.withReentrantLock {
        requirePropertyNotExist(property.name)
        properties[property.name] = property
    }

    override suspend fun minusAssign(propertyId: String): Unit = mutex.withReentrantLock {
        requirePropertyExist(propertyId)
        properties.remove(propertyId)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(propertyId: String): Property<T>? = mutex.withReentrantLock {
        properties[propertyId] as? Property<T>
    }

    override suspend fun <T> updateProperty(property: Property<T>): Unit = mutex.withReentrantLock {
        requirePropertyExist(property.name)
        properties[property.name] = property
    }

    override suspend fun <T> updateProperty(
        name: String,
        transform: (Property<T>) -> Property<T>,
    ): Unit = mutex.withReentrantLock {
        val property = get<T>(name) ?: throw PropertyNotFoundException(name)
        val transformed = transform(property)
        require(transformed.name == name) {
            "Cannot change property name during update. Expected: $name, got: ${transformed.name}"
        }
        properties[name] = transformed
    }

    override suspend fun <T> getOrDefault(
        propertyId: String,
        defaultValue: Property<T>,
    ): Property<T> = mutex.withReentrantLock {
        get(propertyId) ?: defaultValue
    }

    override suspend fun getAll(): Map<String, Property<*>> = mutex.withReentrantLock {
        properties.toMap()
    }

    override suspend fun listPropertyIds(): Set<String> = mutex.withReentrantLock {
        properties.keys.toSet()
    }

    override suspend fun clear() = mutex.withReentrantLock {
        properties.clear()
    }

    /**
     * Checks that a property with the given [name] exists in the store.
     *
     * @param name The name of the property to check.
     * @throws PropertyNotFoundException if the property does not exist.
     * @throws IllegalArgumentException if the property name is blank.
     */
    private fun requirePropertyExist(name: String) {
        require(name.isNotBlank()) { "propertyId cannot be empty" }
        if (name !in properties) {
            throw PropertyNotFoundException(name)
        }
    }

    /**
     * Checks that a property with the given [name] does not exist in the store.
     *
     * @param name The name of the property to check.
     * @throws PropertyAlreadyExistsException if the property already exists.
     * @throws IllegalArgumentException if the property name is blank.
     */
    private fun requirePropertyNotExist(name: String) {
        require(name.isNotBlank()) { "propertyId cannot be empty" }
        if (name in properties) {
            throw PropertyAlreadyExistsException(name)
        }
    }
}
