package com.yonatankarp.ff4k.core

import com.yonatankarp.ff4k.exception.FeatureNotFoundException

/**
 * Storage abstraction for Feature flags with Kotlin-idiomatic operator support.
 *
 * This interface provides a comprehensive API for managing feature flags, including
 * CRUD operations, group management, and permission handling. All operations are
 * suspend functions to support asynchronous I/O across different storage backends.
 *
 * ## Operator Support
 *
 * This interface uses Kotlin operator overloading for common operations:
 * - `featureId in store` - Check if a feature exists
 * - `store[featureId]` - Read a feature
 * - `store += feature` - Create a new feature
 * - `store -= featureId` - Delete a feature
 *
 * Example:
 * ```kotlin
 * val store: FeatureStore = InMemoryFeatureStore()
 *
 * // Create a feature
 * store += Feature("dark-mode", isEnabled = true)
 *
 * // Check existence
 * if ("dark-mode" in store) {
 *     // Read feature
 *     val feature = store["dark-mode"]
 * }
 *
 * // Delete feature
 * store -= "dark-mode"
 * ```
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
interface FeatureStore {

    // ========== Basic Feature CRUD ==========

    /**
     * Enable a feature.
     *
     * @param featureId Unique feature identifier
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun enable(featureId: String)

    /**
     * Disable a feature.
     *
     * @param featureId Unique feature identifier
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun disable(featureId: String)

    /**
     * Check if a feature exists in the store.
     *
     * @param featureId Unique feature identifier
     * @return true if feature exists, false otherwise
     */
    suspend operator fun contains(featureId: String): Boolean

    /**
     * Create a new feature in the store.
     *
     * @param feature Feature to create
     * @throws com.yonatankarp.ff4k.exception.FeatureAlreadyExistsException if feature already exists
     */
    suspend operator fun plusAssign(feature: Feature)

    /**
     * Delete a feature from the store.
     *
     * @param featureId Feature unique identifier
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend operator fun minusAssign(featureId: String)

    /**
     * Update an existing feature in the store.
     *
     * Replaces the stored feature with the provided one. The feature UID must match
     * an existing feature in the store.
     *
     * @param feature Feature with updated values
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun update(feature: Feature)

    /**
     * Read a feature from the store.
     *
     * @param featureId Feature unique identifier
     * @return Feature if found, null otherwise
     */
    suspend operator fun get(featureId: String): Feature?

    /**
     * Read all features from the store.
     *
     * @return Map of feature UID to Feature for all features in the store
     */
    suspend fun getAll(): Map<String, Feature>

    // ========== Group Operations ==========

    /**
     * Enable all features associated with the specified group.
     *
     * @param groupName Group name
     */
    suspend fun enableGroup(groupName: String)

    /**
     * Disable all features associated with the specified group.
     *
     * @param groupName Group name
     */
    suspend fun disableGroup(groupName: String)

    /**
     * Check if a group exists in the store.
     *
     * A group exists if at least one feature is associated with the group name.
     *
     * @param groupName Group name
     * @return true if at least one feature with this group exists, false otherwise
     */
    suspend fun containsGroup(groupName: String): Boolean

    /**
     * Add a feature to a group.
     *
     * Updates the feature's group field to the specified group name.
     * If the feature already belongs to another group, it will be moved to the new group.
     *
     * @param featureId Feature unique identifier
     * @param groupName Group name
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun addToGroup(featureId: String, groupName: String)

    /**
     * Remove a feature from its group.
     *
     * @param featureId Feature unique identifier
     * @param groupName Group name
     * @throws FeatureNotFoundException if feature doesn't exist
     * @throws com.yonatankarp.ff4k.exception.GroupNotFoundException if group doesn't exist or feature is not part of the group
     */
    suspend fun removeFromGroup(featureId: String, groupName: String)

    /**
     * Read all features in a group.
     *
     * @param groupName Group name
     * @return Map of feature UID to Feature for all features in the group or an empty map if the group doesn't exist or has no features.
     */
    suspend fun getGroup(groupName: String): Map<String, Feature>

    /**
     * Get all group names in the store.
     *
     * @return Set of all groups that have at least one feature associated with them.
     */
    suspend fun getAllGroups(): Set<String>

    // ========== Permission Operations ==========

    /**
     * Grant a role/permission to a feature.
     *
     * Adds the specified role to the feature's permissions set. If the feature already
     * has this permission, this operation is idempotent (no error or duplicate).
     *
     * @param featureId Feature unique identifier
     * @param roleName Role/permission name to grant
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun grantRoleToFeature(featureId: String, roleName: String)

    /**
     * Revoke a role/permission from a feature.
     *
     * Removes the specified role from the feature's permissions set. If the feature
     * doesn't have this permission, this operation is idempotent (no error).
     *
     * @param featureId Feature unique identifier
     * @param roleName Role/permission name to revoke
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun revokeRoleFromFeature(featureId: String, roleName: String)

    // ========== Utility Operations ==========

    /**
     * Clear all features from the store.
     *
     * Removes all features permanently. This operation cannot be undone.
     *
     * **WARNING:** This operation is destructive and should be used with caution.
     * Typically used for testing or administrative cleanup operations.
     */
    suspend fun clear()

    /**
     * Check if the store contains no features.
     *
     * @return true if the store has no features, false otherwise
     */
    suspend fun isEmpty(): Boolean

    /**
     * Get the total number of features in the store.
     *
     * @return Count of features in the store
     */
    suspend fun count(): Int

    // ========== Convenience Operations ==========

    /**
     * Update a feature using a transformation function.
     *
     * This is a convenience method that reads the feature, applies the provided
     * transformation, and saves the result back to the store atomically.
     *
     * Implementations should ensure this operation is atomic to prevent race conditions.
     *
     * Example:
     * ```kotlin
     * store.updateFeature("my-feature") { feature ->
     *     feature.copy(isEnabled = feature.isEnabled.not())
     * }
     * ```
     *
     * @param featureId Feature unique identifier
     * @param transform Function to transform the feature
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun updateFeature(
        featureId: String,
        transform: (Feature) -> Feature,
    )

    /**
     * Create or update a feature (upsert operation).
     *
     * If the feature exists, it will be updated. If it doesn't exist, it will be created.
     * This is useful when you don't know whether the feature already exists in the store.
     *
     * Implementations should ensure this operation is atomic to prevent race conditions.
     *
     * Example:
     * ```kotlin
     * store.createOrUpdate(Feature("my-feature", isEnabled = true))
     * ```
     *
     * @param feature Feature to create or update
     */
    suspend fun createOrUpdate(feature: Feature)

    /**
     * Toggle a feature's enabled state.
     *
     * If the feature is currently enabled, it will be disabled, and vice versa.
     *
     * Implementations should ensure this operation is atomic to prevent race conditions.
     *
     * Example:
     * ```kotlin
     * store.toggle("my-feature") // Switches between enabled/disabled
     * ```
     *
     * @param featureId Feature unique identifier
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun toggle(featureId: String)

    /**
     * Read a feature from the store or throw an exception if not found.
     *
     * Unlike the `get` operator which returns null for missing features, this method
     * throws a [FeatureNotFoundException], making it useful when the feature must exist.
     *
     * Example:
     * ```kotlin
     * val feature = store.getOrThrow("my-feature") // Throws if not found
     * ```
     *
     * @param featureId Feature unique identifier
     * @return Feature if found
     * @throws FeatureNotFoundException if feature doesn't exist
     */
    suspend fun getOrThrow(featureId: String): Feature
}
