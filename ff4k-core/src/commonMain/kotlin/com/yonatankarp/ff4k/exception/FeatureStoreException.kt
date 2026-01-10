package com.yonatankarp.ff4k.exception

/**
 * Base exception for all [FeatureStore][com.yonatankarp.ff4k.core.FeatureStore] operations.
 *
 * This sealed exception hierarchy represents errors that can occur during feature store
 * CRUD operations, group management, and permission handling. Being sealed allows for
 * exhaustive when expressions when handling store-specific errors.
 *
 * Common scenarios that throw these exceptions:
 * - Attempting to access non-existent features ([FeatureNotFoundException])
 * - Creating duplicate features ([FeatureAlreadyExistsException])
 * - Operating on non-existent groups ([GroupNotFoundException])
 *
 * @param message The detail message describing the exception
 * @param cause The underlying cause of this exception, or null if none
 *
 * @see com.yonatankarp.ff4k.core.FeatureStore
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
sealed class FeatureStoreException(message: String, cause: Throwable? = null) : FF4kException(message, cause)

/**
 * Thrown when attempting to access a feature that does not exist in the store.
 *
 * This exception is typically thrown by operations such as:
 * - [FeatureStore.get()][com.yonatankarp.ff4k.core.FeatureStore.get] when using helper extensions that throw
 * - [FeatureStore.update()][com.yonatankarp.ff4k.core.FeatureStore.update]
 * - [FeatureStore.minusAssign()][com.yonatankarp.ff4k.core.FeatureStore.minusAssign]
 * - [FeatureStore.enable()][com.yonatankarp.ff4k.core.FeatureStore.enable]
 * - [FeatureStore.disable()][com.yonatankarp.ff4k.core.FeatureStore.disable]
 *
 * Example:
 * ```kotlin
 * try {
 *     featureStore -= featureId
 * } catch (e: FeatureNotFoundException) {
 *     logger.error("Cannot delete feature ${e.message}")
 * }
 * ```
 *
 * @param featureId The unique identifier of the feature that was not found
 *
 * @see com.yonatankarp.ff4k.core.FeatureStore
 */
class FeatureNotFoundException(featureId: String) : FeatureStoreException("Feature not found: $featureId")

/**
 * Thrown when attempting to create a feature that already exists in the store.
 *
 * This exception is thrown by [FeatureStore.plusAssign()][com.yonatankarp.ff4k.core.FeatureStore.plusAssign]
 * when trying to insert a feature with a UID that is already present in the store.
 * To avoid this exception, either check existence first using
 * [FeatureStore.contains()][com.yonatankarp.ff4k.core.FeatureStore.contains] or use the
 * [FeatureStore.createOrUpdate()][com.yonatankarp.ff4k.core.createOrUpdate] extension function.
 *
 * Example:
 * ```kotlin
 * // Safe approach using createOrUpdate extension
 * featureStore.createOrUpdate(feature)
 *
 * // Or check existence first
 * if (feature.uid !in featureStore) {
 *     featureStore += feature
 * }
 * ```
 *
 * @param featureId The unique identifier of the feature that already exists
 *
 * @see com.yonatankarp.ff4k.core.FeatureStore.plusAssign
 * @see com.yonatankarp.ff4k.core.createOrUpdate
 */
class FeatureAlreadyExistsException(featureId: String) : FeatureStoreException("Feature already exists: $featureId")

/**
 * Thrown when attempting to access a feature group that does not exist in the store.
 *
 * This exception may be thrown by group-related operations when no features
 * are associated with the specified group name. Note that some operations like
 * [FeatureStore.getGroup()][com.yonatankarp.ff4k.core.FeatureStore.getGroup]
 * may return an empty map instead of throwing this exception, depending on the
 * implementation.
 *
 * To avoid this exception, use
 * [FeatureStore.containsGroup()][com.yonatankarp.ff4k.core.FeatureStore.containsGroup]
 * to check for group existence first.
 *
 * Example:
 * ```kotlin
 * if (featureStore.containsGroup("beta-features")) {
 *     featureStore.enableGroup("beta-features")
 * }
 * ```
 *
 * @param groupName The name of the group that was not found
 *
 * @see com.yonatankarp.ff4k.core.FeatureStore.containsGroup
 * @see com.yonatankarp.ff4k.core.FeatureStore.getGroup
 */
class GroupNotFoundException(groupName: String) : FeatureStoreException("Group not found: $groupName")
