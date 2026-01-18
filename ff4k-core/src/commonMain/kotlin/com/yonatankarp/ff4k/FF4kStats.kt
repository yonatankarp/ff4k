package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.hasFlippingStrategy
import com.yonatankarp.ff4k.core.hasPermissions

/**
 * Extension functions for [FF4k] providing feature filtering and statistics.
 *
 * This file contains helpers for:
 * - Filtering features by state, permissions, and strategy
 * - Aggregating feature statistics
 * - Generating debug/CLI reports
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */

// ============================================================================
// Feature Statistics Data Class
// ============================================================================

/**
 * Aggregated statistics about features in an [FF4k] instance.
 *
 * Provides a snapshot of the current feature flag state, useful for
 * debugging, monitoring, logging, and CLI tools.
 *
 * Example:
 * ```kotlin
 * val stats = ff4k.stats()
 * println("Total: ${stats.total}, Enabled: ${stats.enabled}, Disabled: ${stats.disabled}")
 * println("Features with strategies: ${stats.withStrategy}")
 * println("Number of groups: ${stats.groups}")
 * ```
 *
 * @property total total number of features
 * @property enabled number of features with isEnabled = true
 * @property disabled number of features with isEnabled = false
 * @property withPermissions number of features with at least one permission
 * @property withStrategy number of features with a flipping strategy
 * @property groups number of distinct groups (excluding features without groups)
 */
data class FeatureStats(
    val total: Int,
    val enabled: Int,
    val disabled: Int,
    val withPermissions: Int,
    val withStrategy: Int,
    val groups: Int,
)

// ============================================================================
// Feature Filtering Extensions
// ============================================================================

/**
 * Get all enabled features.
 *
 * Returns a list of all features where [Feature.isEnabled] is `true`.
 * Does not evaluate flipping strategies - only checks the base enabled state.
 *
 * Example:
 * ```kotlin
 * val enabled = ff4k.enabledFeatures()
 * enabled.forEach { feature ->
 *     println("Enabled: ${feature.uid}")
 * }
 * ```
 *
 * @return list of enabled features
 */
suspend fun FF4k.enabledFeatures(): List<Feature> = features().values.filter { it.isEnabled }

/**
 * Get all disabled features.
 *
 * Returns a list of all features where [Feature.isEnabled] is `false`.
 * Does not evaluate flipping strategies - only checks the base enabled state.
 *
 * Example:
 * ```kotlin
 * val disabled = ff4k.disabledFeatures()
 * disabled.forEach { feature ->
 *     println("Disabled: ${feature.uid}")
 * }
 * ```
 *
 * @return list of disabled features
 */
suspend fun FF4k.disabledFeatures(): List<Feature> = features().values.filter { !it.isEnabled }

/**
 * Get features with a specific permission.
 *
 * Returns a list of all features that have the specified permission
 * in their [Feature.permissions] set.
 *
 * Example:
 * ```kotlin
 * val adminFeatures = ff4k.featuresWithPermission("ADMIN")
 * adminFeatures.forEach { feature ->
 *     println("Admin-restricted: ${feature.uid}")
 * }
 * ```
 *
 * @param permission the permission to filter by
 * @return list of features with the specified permission
 */
suspend fun FF4k.featuresWithPermission(permission: String): List<Feature> = features().values.filter { permission in it.permissions }

/**
 * Get features with a flipping strategy.
 *
 * Returns a list of all features that have a [com.yonatankarp.ff4k.core.FlippingStrategy]
 * configured. These features use advanced activation logic beyond simple on/off.
 *
 * Example:
 * ```kotlin
 * val strategyFeatures = ff4k.featuresWithStrategy()
 * strategyFeatures.forEach { feature ->
 *     println("${feature.uid}: ${feature.displayStrategyClassName}")
 * }
 * ```
 *
 * @return list of features with a flipping strategy
 */
suspend fun FF4k.featuresWithStrategy(): List<Feature> = features().values.filter { it.hasFlippingStrategy }

// ============================================================================
// Statistics and Reporting Extensions
// ============================================================================

/**
 * Get statistics about features.
 *
 * Returns an aggregated [FeatureStats] snapshot of the current feature state.
 * Useful for monitoring, debugging, and reporting.
 *
 * Example:
 * ```kotlin
 * val stats = ff4k.stats()
 * println("Total features: ${stats.total}")
 * println("Enabled: ${stats.enabled} / Disabled: ${stats.disabled}")
 * println("With permissions: ${stats.withPermissions}")
 * println("With strategy: ${stats.withStrategy}")
 * println("Groups: ${stats.groups}")
 * ```
 *
 * @return aggregated feature statistics
 */
suspend fun FF4k.stats(): FeatureStats {
    val allFeatures = features().values
    return FeatureStats(
        total = allFeatures.size,
        enabled = allFeatures.count { it.isEnabled },
        disabled = allFeatures.count { !it.isEnabled },
        withPermissions = allFeatures.count { it.hasPermissions },
        withStrategy = allFeatures.count { it.hasFlippingStrategy },
        groups = allFeatures.mapNotNull { it.group }.distinct().size,
    )
}

/**
 * Get a summary report of all features.
 *
 * Returns a human-readable string representation of the feature state,
 * useful for debugging, CLI tools, and log output.
 *
 * The report format:
 * ```
 * FF4K Feature Report
 * ===================
 * Total: 5 | Enabled: 3 | Disabled: 2
 * With Permissions: 2 | With Strategy: 1 | Groups: 2
 *
 * Features:
 *   [ON]  dark-mode (group: ui)
 *   [OFF] beta-feature (group: ui) [STRATEGY: PercentageStrategy]
 *   [ON]  premium (group: billing) [PERMISSIONS: ADMIN, PREMIUM]
 *   [OFF] legacy
 *   [ON]  new-checkout [STRATEGY: RegionStrategy] [PERMISSIONS: BETA]
 * ```
 *
 * @return formatted feature report string
 */
suspend fun FF4k.report(): String {
    val allFeatures = features().values.sortedBy { it.uid }
    val stats = stats()

    return buildString {
        appendLine("FF4K Feature Report")
        appendLine("===================")
        appendLine("Total: ${stats.total} | Enabled: ${stats.enabled} | Disabled: ${stats.disabled}")
        appendLine("With Permissions: ${stats.withPermissions} | With Strategy: ${stats.withStrategy} | Groups: ${stats.groups}")
        appendLine()
        appendLine("Features:")

        if (allFeatures.isEmpty()) {
            appendLine("  (no features)")
        } else {
            allFeatures.forEach { feature ->
                val status = if (feature.isEnabled) "[ON] " else "[OFF]"
                val group = feature.group?.let { " (group: $it)" } ?: ""
                val strategy = if (feature.hasFlippingStrategy) " [STRATEGY: ${feature.displayStrategyClassName}]" else ""
                val permissions = if (feature.hasPermissions) " [PERMISSIONS: ${feature.permissions.joinToString(", ")}]" else ""
                appendLine("  $status ${feature.uid}$group$strategy$permissions")
            }
        }
    }.trimEnd()
}
