package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.property.Property

/**
 * A store for managing feature flag properties.
 *
 * PropertyStore provides a collection-like interface for storing and retrieving
 * properties with support for operator overloading and suspending operations.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
interface PropertyStore {

    /**
     * Checks whether the store is empty.
     *
     * @return `true` if the store contains no properties, `false` otherwise
     */
    suspend fun isEmpty(): Boolean

    /**
     * Checks if a property with the given id exists in the store.
     *
     * Usage: `if ("featureName" in store) { ... }`
     *
     * @param propertyId the id of the property to check
     * @return `true` if a property with the given id exists, `false` otherwise
     */
    suspend operator fun contains(propertyId: String): Boolean

    /**
     * Adds a new property to the store.
     *
     * Usage: `store += property`
     *
     * @param property the property to add to the store
     */
    suspend operator fun <T> plusAssign(property: Property<T>)

    /**
     * Removes a property from the store by id.
     *
     * Usage: `store -= "propertyName"`
     *
     * @param propertyId the id of the property to remove
     */
    suspend operator fun minusAssign(propertyId: String)

    /**
     * Retrieves a property by id.
     *
     * Usage: `val property = store["propertyName"]`
     *
     * @param propertyId the id of the property to retrieve
     * @return the property with the given id, or `null` if not found
     */
    suspend operator fun <T> get(propertyId: String): Property<T>?

    /**
     * Updates an existing property in the store.
     *
     * This operation is semantically different from adding a property
     * and should only be used when modifying an existing property.
     *
     * @param property the property with updated values
     */
    suspend fun <T> updateProperty(property: Property<T>)

    /**
     * Atomically updates a property using a transformation function.
     *
     * This method ensures that the read-modify-write cycle is atomic, preventing
     * race conditions when multiple coroutines update the same property.
     *
     * @param name the name of the property to update
     * @param transform the function to apply to the current property value
     * @throws com.yonatankarp.ff4k.exception.PropertyNotFoundException if the property does not exist
     */
    suspend fun <T> updateProperty(name: String, transform: (Property<T>) -> Property<T>)

    /**
     * Retrieves a property by id, or returns a default if not found.
     *
     * @param propertyId the id of the property to retrieve
     * @param defaultValue the default property to return if the id property is not found
     * @return the property with the given id, or [defaultValue] if not found
     */
    suspend fun <T> getOrDefault(
        propertyId: String,
        defaultValue: Property<T>,
    ): Property<T>

    /**
     * Retrieves all properties in the store.
     *
     * @return a map of property ids to their corresponding properties
     */
    suspend fun getAll(): Map<String, Property<*>>

    /**
     * Lists all property ids in the store.
     *
     * @return a set of all property ids
     */
    suspend fun listPropertyIds(): Set<String>

    /**
     * Removes all properties from the store.
     */
    suspend fun clear()
}
