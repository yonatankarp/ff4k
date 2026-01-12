package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.addGroup
import com.yonatankarp.ff4k.core.grantPermissions
import com.yonatankarp.ff4k.core.removeGroup
import com.yonatankarp.ff4k.core.revokePermissions
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.exception.GroupNotFoundException

/**
 * Base implementation of [FeatureStore] that provides common functionality.
 *
 * This class implements many of the default behaviors for a feature store,
 * allowing specific implementations to focus on the storage mechanism itself.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class AbstractFeatureStore : FeatureStore {

    override suspend fun enable(featureId: String) {
        updateFeature(featureId) { it.enable() }
    }

    override suspend fun disable(featureId: String) {
        updateFeature(featureId) { it.disable() }
    }

    override suspend fun grantRoleToFeature(featureId: String, roleName: String) {
        require(roleName.isNotBlank()) { "roleName (#1) cannot be empty" }
        updateFeature(featureId) { it.grantPermissions(roleName) }
    }

    override suspend fun revokeRoleFromFeature(featureId: String, roleName: String) {
        require(roleName.isNotBlank()) { "roleName (#1) cannot be empty" }
        updateFeature(featureId) { it.revokePermissions(roleName) }
    }

    override suspend fun getGroup(groupName: String): Map<String, Feature> {
        require(groupName.isNotBlank()) { "groupName (#0) cannot be empty" }
        return getAll()
            .filterValues { it.group == groupName }
    }

    override suspend fun containsGroup(groupName: String): Boolean = getAll().values.any { it.group == groupName }

    override suspend fun enableGroup(groupName: String) = updateGroupFeatures(groupName) { it.enable() }

    override suspend fun disableGroup(groupName: String) = updateGroupFeatures(groupName) { it.disable() }

    private suspend inline fun updateGroupFeatures(
        groupName: String,
        crossinline transform: (Feature) -> Feature,
    ) {
        getGroup(groupName).keys.forEach { featureId ->
            try {
                updateFeature(featureId) { feature ->
                    if (feature.group == groupName) {
                        transform(feature)
                    } else {
                        feature
                    }
                }
            } catch (_: FeatureNotFoundException) {
                // Feature was removed concurrently - ignore
            }
        }
    }

    override suspend fun addToGroup(featureId: String, groupName: String) {
        require(groupName.isNotBlank()) { "groupName (#1) cannot be empty" }
        updateFeature(featureId) { it.addGroup(groupName) }
    }

    override suspend fun removeFromGroup(featureId: String, groupName: String) {
        require(groupName.isNotBlank()) { "groupName cannot be empty" }

        updateFeature(featureId) { feature ->
            if (feature.group != groupName) {
                throw GroupNotFoundException(groupName)
            }
            feature.removeGroup()
        }
    }

    override suspend fun getAllGroups(): Set<String> = getAll().values
        .mapNotNull { it.group }
        .filter { it.isNotBlank() }
        .toSet()
}
