package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.core.PropertyStore
import com.yonatankarp.ff4k.core.currentFlippingContext
import com.yonatankarp.ff4k.core.withFlippingContext
import com.yonatankarp.ff4k.exception.FeatureNotFoundException
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.store.InMemoryPropertyStore
import com.yonatankarp.ff4k.utils.withReentrantLock
import kotlinx.coroutines.sync.Mutex

/**
 * The primary entry point and facade for the FF4K feature flag library.
 *
 * FF4K provides a coroutine-safe, Kotlin Multiplatform feature flag and configuration
 * management system. This class serves as the main API for all feature flag operations,
 * delegating storage concerns to pluggable [FeatureStore] and [PropertyStore] implementations.
 *
 * ## Key Features
 *
 * - **Feature Flags**: Enable, disable, and check feature states with optional flipping strategies
 * - **Properties**: Type-safe configuration properties with support for various data types
 * - **Groups**: Organize features into logical groups for bulk operations
 * - **Auto-Create Mode**: Automatically create missing features on first access
 * - **Flipping Strategies**: Conditional feature activation based on runtime context
 *
 * ## Architecture
 *
 * FF4K follows a facade pattern, providing a unified API while delegating to:
 * - [FeatureStore]: Manages feature flag persistence and retrieval
 * - [PropertyStore]: Manages configuration property persistence and retrieval
 *
 * Both stores default to in-memory implementations but can be replaced with persistent
 * backends (Redis, databases, etc.) by implementing the respective interfaces.
 *
 * ## Thread Safety
 *
 * All operations are suspend functions designed for use with Kotlin coroutines.
 * Internal state is protected using [Mutex] for coroutine-safe concurrent access.
 * The stores themselves are responsible for their own thread-safety guarantees.
 *
 * ## Context Resolution
 *
 * When checking features with [FlippingStrategy], context is resolved in priority order:
 * 1. Explicit [FlippingExecutionContext] passed to [check]
 * 2. Implicit context from [withFlippingContext] coroutine scope
 * 3. Empty context (when neither is provided)
 *
 * ## Example Usage
 *
 * ### Basic Feature Checking
 * ```kotlin
 * val ff4k = FF4k()
 *
 * // Add and check features
 * ff4k.addFeature("dark-mode", isEnabled = true)
 * if (ff4k.check("dark-mode")) {
 *     enableDarkMode()
 * }
 *
 * // Toggle features
 * ff4k.enable("beta-feature")
 * ff4k.disable("legacy-feature")
 * ```
 *
 * ### Using Flipping Strategies with Context
 * ```kotlin
 * // Feature with percentage rollout strategy
 * val feature = Feature(
 *     uid = "new-checkout",
 *     isEnabled = true,
 *     flippingStrategy = PercentageStrategy(mapOf("percentage" to "25"))
 * )
 * ff4k.addFeature(feature)
 *
 * // Check with explicit context
 * val context = FlippingExecutionContext("userId" to "user-123")
 * val isEnabled = ff4k.check("new-checkout", context)
 *
 * // Or use coroutine context propagation
 * withFlippingContext(FlippingExecutionContext("userId" to "user-123")) {
 *     val isEnabled = ff4k.check("new-checkout") // context automatically available
 * }
 * ```
 *
 * ### Working with Groups
 * ```kotlin
 * ff4k.addFeature(Feature("feature-a", isEnabled = false, group = "experiment-1"))
 * ff4k.addFeature(Feature("feature-b", isEnabled = false, group = "experiment-1"))
 *
 * // Enable all features in a group at once
 * ff4k.enableGroup("experiment-1")
 *
 * // Query features by group
 * val experimentFeatures = ff4k.featuresByGroup("experiment-1")
 * ```
 *
 * ### Auto-Create Mode
 * ```kotlin
 * // With autoCreate enabled, missing features are created on first access
 * val ff4k = FF4k(autoCreate = true)
 *
 * // This won't throw - creates a disabled feature automatically
 * val isEnabled = ff4k.check("non-existent-feature") // returns false
 * ```
 *
 * ### Configuration Properties
 * ```kotlin
 * ff4k.addProperty(PropertyString("api.url", "https://api.example.com"))
 * ff4k.addProperty(PropertyInt("max.retries", 3))
 *
 * val apiUrl: Property<String>? = ff4k.property("api.url")
 * val maxRetries: String? = ff4k.propertyAsString<Int>("max.retries")
 * ```
 *
 * @property featureStore The store managing feature flag persistence. Defaults to [InMemoryFeatureStore].
 * @property propertyStore The store managing property persistence. Defaults to [InMemoryPropertyStore].
 * @property source Identifies the source/client type making API calls. Used for auditing and analytics.
 * @property autoCreate When `true`, automatically creates missing features as disabled on first access.
 *                      When `false`, throws [FeatureNotFoundException] for missing features.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 * @see FeatureStore
 * @see PropertyStore
 * @see Feature
 * @see FlippingStrategy
 * @see withFlippingContext
 */
class FF4k(
    val featureStore: FeatureStore = InMemoryFeatureStore(),
    val propertyStore: PropertyStore = InMemoryPropertyStore(),
    val source: Source = Source.KotlinApi,
    val autoCreate: Boolean = false,
) {

    private val mutex = Mutex()

    /**
     * Describes the origin or integration point from which this [FF4k] instance is accessed.
     *
     * This can be used by applications for logging, monitoring, auditing or routing purposes
     * (for example, to distinguish traffic coming from an embedded servlet endpoint versus
     * direct Kotlin API usage).
     *
     * Within [FF4k] itself, this value is currently informational and does not change the
     * feature‑flag evaluation behavior. Callers are free to use it to adapt their own
     * surrounding infrastructure or conventions.
     */
    enum class Source {
        /**
         * Indicates that [FF4k] is being used directly from Kotlin code, for example from
         * application services, scheduled jobs, or other in‑process components.
         *
         * Choose this when your code calls the `FF4k` API directly without going through
         * an HTTP, servlet, or SSH boundary.
         */
        KotlinApi,

        /**
         * Indicates that [FF4k] is accessed via an embedded servlet container (such as
         * running inside a web framework that exposes servlet endpoints).
         *
         * Choose this when feature operations are primarily triggered through requests
         * handled by an embedded servlet in your application.
         */
        EmbeddedServlet,

        /**
         * Indicates that [FF4k] is exposed through a web API endpoint (for example, a
         * REST/HTTP or other web‑based protocol).
         *
         * Choose this when feature operations are invoked over a remote web API rather
         * than directly from in‑process Kotlin code.
         */
        WebApi,

        /**
         * Indicates that [FF4k] is accessed via an SSH‑based interface or tooling.
         *
         * Choose this when feature operations are executed over an SSH channel (for
         * example, from administrative scripts or remote consoles).
         */
        Ssh,
    }

    /**
     * Check if a feature is enabled.
     *
     * Context resolution (in priority order):
     * 1. Explicit [executionContext] parameter (if provided)
     * 2. Implicit context from [withFlippingContext] coroutine scope
     * 3. Empty context (no parameters)
     *
     * @param featureId Feature unique identifier
     * @param executionContext Optional explicit context (overrides implicit context)
     * @return true if feature is enabled and all strategy checks pass, false otherwise
     */
    suspend fun check(
        featureId: String,
        executionContext: FlippingExecutionContext? = null,
    ): Boolean {
        val feature = feature(featureId)
        var flipped = feature.isEnabled

        // Strategy Evaluation
        if (flipped && feature.flippingStrategy != null) {
            val effectiveContext = executionContext ?: currentFlippingContext()
            flipped = feature.flippingStrategy.evaluate(featureId, featureStore(), effectiveContext)
        }

        return flipped
    }

    /*==========================*
     *     Feature Management   *
     *==========================*/

    suspend fun features(): Map<String, Feature> = featureStore().getAll()

    suspend fun feature(featureId: String): Feature = featureStore().getOrAutoCreate(
        featureId = featureId,
        autoCreate = autoCreate,
        lock = mutex,
        createDefault = { Feature(featureId, isEnabled = false) },
    ) { getOrThrow(featureId) }

    suspend fun featuresByGroup(groupName: String): Map<String, Feature> = featureStore().getGroup(groupName)

    suspend fun enable(featureId: String) = apply {
        featureStore().getOrAutoCreate(
            featureId = featureId,
            autoCreate = autoCreate,
            lock = mutex,
            createDefault = { Feature(featureId, isEnabled = true) },
        ) {
            enable(featureId)
            getOrThrow(featureId)
        }
    }

    suspend fun disable(featureId: String) = apply {
        featureStore().getOrAutoCreate(
            featureId = featureId,
            autoCreate = autoCreate,
            lock = mutex,
            createDefault = { Feature(featureId, isEnabled = false) },
        ) {
            disable(featureId)
            getOrThrow(featureId)
        }
    }

    suspend fun hasFeature(featureId: String): Boolean = featureId in featureStore()
    suspend fun containGroup(groupName: String): Boolean = featureStore().containsGroup(groupName)

    /*==========================*
     *    Property Management   *
     *==========================*/

    suspend fun properties(): Map<String, Property<*>> = propertyStore().getAll()

    suspend fun <T> property(propertyId: String): Property<T>? = propertyStore()[propertyId]

    suspend fun <T> propertyAsString(propertyId: String): String? = propertyStore().get<T>(propertyId)?.value?.toString()

    suspend fun hasProperty(propertyId: String): Boolean = propertyId in propertyStore()

    /*==========================*
     *          Builder         *
     *==========================*/

    suspend fun addFeature(feature: Feature) = apply {
        featureStore() += feature
    }

    suspend fun addFeature(featureId: String, isEnabled: Boolean = false, description: String? = null) = apply {
        addFeature(Feature(featureId, isEnabled, description))
    }

    suspend fun deleteFeature(feature: Feature) = apply {
        featureStore() -= feature.uid
    }

    suspend fun <T> addProperty(property: Property<T>) = apply {
        propertyStore() += property
    }

    suspend fun <T> deleteProperty(property: Property<T>) = apply {
        propertyStore() -= property.name
    }

    suspend fun enableGroup(groupName: String) = apply {
        featureStore().enableGroup(groupName)
    }

    suspend fun disableGroup(groupName: String) = apply {
        featureStore().disableGroup(groupName)
    }

    /*==========================*
     *          Helper          *
     *==========================*/

    private suspend fun featureStore() = featureStore
    private suspend fun propertyStore() = propertyStore

    /**
     * Attempts to execute an operation, auto-creating the feature if it doesn't exist.
     *
     * When [autoCreate] is enabled and the feature is not found:
     * 1. Acquires the lock to prevent concurrent auto-creation
     * 2. Double-checks if the feature still doesn't exist (another coroutine may have created it)
     * 3. If missing, creates the feature using [createDefault] and returns it directly
     * 4. If another coroutine created it while waiting for the lock, executes [operation]
     *
     * @param featureId The feature identifier to look up or create
     * @param autoCreate Whether to auto-create missing features
     * @param lock Mutex for synchronizing auto-creation
     * @param createDefault Factory function to create the default feature
     * @param operation The operation to execute (used for initial lookup and when feature already exists)
     * @return The feature from the operation or the newly created feature
     * @throws FeatureNotFoundException if the feature doesn't exist and [autoCreate] is false
     */
    private suspend inline fun FeatureStore.getOrAutoCreate(
        featureId: String,
        autoCreate: Boolean,
        lock: Mutex,
        crossinline createDefault: suspend (String) -> Feature,
        crossinline operation: suspend FeatureStore.() -> Feature,
    ): Feature = try {
        operation()
    } catch (e: FeatureNotFoundException) {
        if (autoCreate.not()) throw e
        lock.withReentrantLock {
            if (featureId in this) {
                operation()
            } else {
                val feature = createDefault(featureId)
                this += feature
                feature
            }
        }
    }
}
