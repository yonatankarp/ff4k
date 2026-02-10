package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.exception.PropertyAlreadyExistsException
import com.yonatankarp.ff4k.exception.PropertyNotFoundException
import com.yonatankarp.ff4k.property.Property

/**
 * Base implementation of [PropertyStore] that provides common functionality.
 *
 * This class implements default behaviors for a property store, allowing
 * specific implementations to focus on the storage mechanism itself.
 */
abstract class AbstractPropertyStore : PropertyStore {
    /**
     * Checks that a property with the given [name] exists in the store.
     *
     * @param name The name of the property to check.
     * @throws PropertyNotFoundException if the property does not exist.
     * @throws IllegalArgumentException if the property name is blank.
     */
    protected abstract suspend fun requirePropertyExist(name: String)

    /**
     * Checks that a property with the given [name] does not exist in the store.
     *
     * @param name The name of the property to check.
     * @throws PropertyAlreadyExistsException if the property already exists.
     * @throws IllegalArgumentException if the property name is blank.
     */
    protected abstract suspend fun requirePropertyNotExist(name: String)

    /**
     * Creates a new property or updates an existing one atomically.
     *
     * Subclasses must implement this with atomic semantics to avoid
     * TOCTOU (time-of-check-time-of-use) race conditions.
     *
     * @param property The property to create or update.
     */
    abstract override suspend fun <T> createOrUpdate(property: Property<T>)
}
