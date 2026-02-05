package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.core.ContextKeys
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FeatureStore
import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.dsl.core.ff4k
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

/**
 * Tests for FF4k conditional execution and batch check extension functions.
 */
internal class FF4kExtensionsTest :
    FunSpec({

        context("ifEnabled") {
            withData(
                nameFn = { it.description },
                ifEnabledData,
            ) { (_, featuresBlock, context, expected) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val result = if (context != null) {
                    ff4k.ifEnabled(FEATURE_TEST, context) { VALUE_EXECUTED }
                } else {
                    ff4k.ifEnabled(FEATURE_TEST) { VALUE_EXECUTED }
                }

                // Then
                result shouldBe expected
            }
        }

        context("ifEnabledOrElse") {
            withData(
                nameFn = { it.description },
                ifEnabledOrElseData,
            ) { (_, featuresBlock, context, expected) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }

                // When
                val result = if (context != null) {
                    ff4k.ifEnabledOrElse(
                        FEATURE_TEST,
                        context,
                        enabled = { VALUE_ENABLED },
                        disabled = { VALUE_DISABLED },
                    )
                } else {
                    ff4k.ifEnabledOrElse(
                        FEATURE_TEST,
                        enabled = { VALUE_ENABLED },
                        disabled = { VALUE_DISABLED },
                    )
                }

                // Then
                result shouldBe expected
            }
        }

        context("whenEnabled") {
            withData(
                nameFn = { it.description },
                whenEnabledData,
            ) { (_, featuresBlock, context, expected) ->
                // Given
                val ff4k = ff4k {
                    features(featuresBlock)
                }
                var executed = false

                // When
                if (context != null) {
                    ff4k.whenEnabled(FEATURE_TEST, context) { executed = true }
                } else {
                    ff4k.whenEnabled(FEATURE_TEST) { executed = true }
                }

                // Then
                executed shouldBe expected
            }
        }

        context("checkAll") {
            withData(
                nameFn = { it.description },
                checkAllData,
            ) { (_, featuresList, featureIdsToCheck, context, expected) ->
                // Given
                val ff4k = ff4k {
                    features {
                        features(featuresList)
                    }
                }

                // When
                val result = ff4k.checkAll(
                    *featureIdsToCheck.toTypedArray(),
                    executionContext = context,
                )

                // Then
                result shouldBe expected
            }
        }

        context("checkAny") {
            withData(
                nameFn = { it.description },
                checkAnyData,
            ) { (_, featuresList, featureIdsToCheck, context, expected) ->
                // Given
                val ff4k = ff4k {
                    features {
                        features(featuresList)
                    }
                }

                // When
                val result = ff4k.checkAny(
                    *featureIdsToCheck.toTypedArray(),
                    executionContext = context,
                )

                // Then
                result shouldBe expected
            }
        }
    }) {
    companion object {
        // Feature IDs
        private const val FEATURE_TEST = "test-feature"
        private const val FEATURE_ONE = "feature-1"
        private const val FEATURE_TWO = "feature-2"
        private const val FEATURE_THREE = "feature-3"

        // Values
        private const val VALUE_EXECUTED = "executed"
        private const val VALUE_ENABLED = "enabled"
        private const val VALUE_DISABLED = "disabled"

        // Context keys and values
        private const val TARGET_USER_ID = "user-123"
        private const val OTHER_USER_ID = "user-456"

        private val ifEnabledData = listOf(
            ExtensionCheckData(
                description = "executes block when feature is enabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = true } },
                expected = VALUE_EXECUTED,
            ),
            ExtensionCheckData(
                description = "returns null when feature is disabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = false } },
                expected = null,
            ),
            ExtensionCheckData(
                description = "executes block when strategy matches context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to TARGET_USER_ID),
                expected = VALUE_EXECUTED,
            ),
            ExtensionCheckData(
                description = "returns null when strategy does not match context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to OTHER_USER_ID),
                expected = null,
            ),
        )

        private val ifEnabledOrElseData = listOf(
            ExtensionCheckData(
                description = "executes enabled block when feature is enabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = true } },
                expected = VALUE_ENABLED,
            ),
            ExtensionCheckData(
                description = "executes disabled block when feature is disabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = false } },
                expected = VALUE_DISABLED,
            ),
            ExtensionCheckData(
                description = "executes enabled block when strategy matches context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to TARGET_USER_ID),
                expected = VALUE_ENABLED,
            ),
            ExtensionCheckData(
                description = "executes disabled block when strategy does not match context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to OTHER_USER_ID),
                expected = VALUE_DISABLED,
            ),
        )

        private val whenEnabledData = listOf(
            ExtensionCheckData(
                description = "executes block when feature is enabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = true } },
                expected = true,
            ),
            ExtensionCheckData(
                description = "does not execute block when feature is disabled",
                featuresBlock = { feature(FEATURE_TEST) { isEnabled = false } },
                expected = false,
            ),
            ExtensionCheckData(
                description = "executes block when strategy matches context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to TARGET_USER_ID),
                expected = true,
            ),
            ExtensionCheckData(
                description = "does not execute block when strategy does not match context",
                featuresBlock = {
                    feature(FEATURE_TEST) {
                        isEnabled = true
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID)
                    }
                },
                context = FlippingExecutionContext(ContextKeys.USER_ID to OTHER_USER_ID),
                expected = false,
            ),
        )

        private val checkAllData = listOf(
            CheckData(
                description = "returns true when all features are enabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = true),
                    Feature(FEATURE_TWO, isEnabled = true),
                    Feature(FEATURE_THREE, isEnabled = true),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE),
                expected = true,
            ),
            CheckData(
                description = "returns false when any feature is disabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = true),
                    Feature(FEATURE_TWO, isEnabled = false),
                    Feature(FEATURE_THREE, isEnabled = true),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE),
                expected = false,
            ),
            CheckData(
                description = "returns false when all features are disabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = false),
                    Feature(FEATURE_TWO, isEnabled = false),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                expected = false,
            ),
            CheckData(
                description = "returns true for empty feature list",
                features = emptyList(),
                featureIdsToCheck = emptyList(),
                expected = true,
            ),
            CheckData(
                description = "returns true when strategy matches context for all features",
                features = listOf(
                    Feature(
                        FEATURE_ONE,
                        isEnabled = true,
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID),
                    ),
                    Feature(FEATURE_TWO, isEnabled = true),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                context = FlippingExecutionContext(ContextKeys.USER_ID to TARGET_USER_ID),
                expected = true,
            ),
            CheckData(
                description = "returns false when strategy does not match context",
                features = listOf(
                    Feature(
                        FEATURE_ONE,
                        isEnabled = true,
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID),
                    ),
                    Feature(FEATURE_TWO, isEnabled = true),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                context = FlippingExecutionContext(ContextKeys.USER_ID to OTHER_USER_ID),
                expected = false,
            ),
        )

        private val checkAnyData = listOf(
            CheckData(
                description = "returns true when all features are enabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = true),
                    Feature(FEATURE_TWO, isEnabled = true),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                expected = true,
            ),
            CheckData(
                description = "returns true when at least one feature is enabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = false),
                    Feature(FEATURE_TWO, isEnabled = true),
                    Feature(FEATURE_THREE, isEnabled = false),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO, FEATURE_THREE),
                expected = true,
            ),
            CheckData(
                description = "returns false when all features are disabled",
                features = listOf(
                    Feature(FEATURE_ONE, isEnabled = false),
                    Feature(FEATURE_TWO, isEnabled = false),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                expected = false,
            ),
            CheckData(
                description = "returns false for empty feature list",
                features = emptyList(),
                featureIdsToCheck = emptyList(),
                expected = false,
            ),
            CheckData(
                description = "returns true when strategy matches context for at least one feature",
                features = listOf(
                    Feature(
                        FEATURE_ONE,
                        isEnabled = true,
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID),
                    ),
                    Feature(FEATURE_TWO, isEnabled = false),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                context = FlippingExecutionContext(ContextKeys.USER_ID to TARGET_USER_ID),
                expected = true,
            ),
            CheckData(
                description = "returns false when strategy does not match context for any feature",
                features = listOf(
                    Feature(
                        FEATURE_ONE,
                        isEnabled = true,
                        flippingStrategy = UserIdStrategy(TARGET_USER_ID),
                    ),
                    Feature(FEATURE_TWO, isEnabled = false),
                ),
                featureIdsToCheck = listOf(FEATURE_ONE, FEATURE_TWO),
                context = FlippingExecutionContext(ContextKeys.USER_ID to OTHER_USER_ID),
                expected = false,
            ),
        )
    }

    /**
     * Simple flipping strategy that checks if the context contains a specific user ID.
     */
    private class UserIdStrategy(private val allowedUserId: String) : FlippingStrategy {
        override suspend fun evaluate(
            featureId: String,
            store: FeatureStore?,
            context: FlippingExecutionContext,
        ): Boolean {
            val userId = context.get<String>(ContextKeys.USER_ID)
            return userId == allowedUserId
        }
    }

    private data class CheckData(
        val description: String,
        val features: List<Feature>,
        val featureIdsToCheck: List<String>,
        val context: FlippingExecutionContext? = null,
        val expected: Boolean,
    )

    private data class ExtensionCheckData<T>(
        val description: String,
        val featuresBlock: com.yonatankarp.ff4k.dsl.feature.FeaturesBuilder.() -> Unit,
        val context: FlippingExecutionContext? = null,
        val expected: T,
    )
}
