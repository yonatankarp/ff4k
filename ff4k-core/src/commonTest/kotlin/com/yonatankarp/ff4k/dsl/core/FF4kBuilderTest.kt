package com.yonatankarp.ff4k.dsl.core

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import com.yonatankarp.ff4k.store.InMemoryPropertyStore
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests for FF4kBuilder and ff4k() DSL entry point.
 *
 * @author Yonatan Karp-Rudin
 */
internal class FF4kBuilderTest :
    FunSpec({

        test("ff4k creates empty instance when no configuration provided") {
            // When
            val ff4k = ff4k { }

            // Then
            ff4k.features().shouldBeEmpty()
            ff4k.properties().shouldBeEmpty()
        }

        test("ff4k registers pre-built feature using feature method") {
            // Given
            val feature = Feature(FEATURE_DARK_MODE, isEnabled = true)

            // When
            val ff4k = ff4k {
                feature(feature)
            }

            // Then
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
        }

        test("ff4k registers pre-built property using property method") {
            // Given
            val property = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

            // When
            val ff4k = ff4k {
                property(property)
            }

            // Then
            ff4k.hasProperty(PROPERTY_API_URL).shouldBeTrue()
            ff4k.property<String>(PROPERTY_API_URL)?.value shouldBe VALUE_API_URL
        }

        test("ff4k registers multiple features using features block") {
            // When
            val ff4k = ff4k {
                features {
                    feature(FEATURE_DARK_MODE) {
                        isEnabled = true
                        description = DESCRIPTION_DARK_MODE
                    }
                    feature(FEATURE_BETA) {
                        isEnabled = false
                        group = GROUP_EXPERIMENTAL
                    }
                }
            }

            // Then
            ff4k.features().size shouldBe 2
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.check(FEATURE_BETA).shouldBeFalse()
            ff4k.feature(FEATURE_DARK_MODE).description shouldBe DESCRIPTION_DARK_MODE
            ff4k.feature(FEATURE_BETA).group shouldBe GROUP_EXPERIMENTAL
        }

        test("ff4k registers multiple properties using properties block") {
            // When
            val ff4k = ff4k {
                properties {
                    property(PROPERTY_MAX_RETRIES) {
                        value = VALUE_MAX_RETRIES
                        description = DESCRIPTION_MAX_RETRIES
                    }
                    property(PROPERTY_TIMEOUT_MS) {
                        value = VALUE_TIMEOUT_MS
                    }
                }
            }

            // Then
            ff4k.properties().size shouldBe 2
            ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.value shouldBe VALUE_MAX_RETRIES
            ff4k.property<Long>(PROPERTY_TIMEOUT_MS)?.value shouldBe VALUE_TIMEOUT_MS
            ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.description shouldBe DESCRIPTION_MAX_RETRIES
        }

        test("ff4k combines pre-built and DSL-defined features") {
            // Given
            val preBuiltFeature = Feature(FEATURE_PREMIUM, isEnabled = true)

            // When
            val ff4k = ff4k {
                feature(preBuiltFeature)
                features {
                    feature(FEATURE_DARK_MODE) {
                        isEnabled = true
                    }
                }
            }

            // Then
            ff4k.features().size shouldBe 2
            ff4k.hasFeature(FEATURE_PREMIUM).shouldBeTrue()
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeTrue()
        }

        test("ff4k combines pre-built and DSL-defined properties") {
            // Given
            val preBuiltProperty = PropertyString(PROPERTY_API_URL, VALUE_API_URL)

            // When
            val ff4k = ff4k {
                property(preBuiltProperty)
                properties {
                    property(PROPERTY_MAX_RETRIES) {
                        value = VALUE_MAX_RETRIES
                    }
                }
            }

            // Then
            ff4k.properties().size shouldBe 2
            ff4k.hasProperty(PROPERTY_API_URL).shouldBeTrue()
            ff4k.hasProperty(PROPERTY_MAX_RETRIES).shouldBeTrue()
        }

        test("ff4k accepts custom feature store") {
            // Given
            val customStore = InMemoryFeatureStore()

            // When
            val ff4k = ff4k(featureStore = customStore) {
                features {
                    feature(FEATURE_DARK_MODE) {
                        isEnabled = true
                    }
                }
            }

            // Then
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeTrue()
            (FEATURE_DARK_MODE in customStore).shouldBeTrue()
        }

        test("ff4k accepts custom property store") {
            // Given
            val customStore = InMemoryPropertyStore()

            // When
            val ff4k = ff4k(propertyStore = customStore) {
                properties {
                    property(PROPERTY_MAX_RETRIES) {
                        value = VALUE_MAX_RETRIES
                    }
                }
            }

            // Then
            ff4k.hasProperty(PROPERTY_MAX_RETRIES).shouldBeTrue()
            (PROPERTY_MAX_RETRIES in customStore).shouldBeTrue()
        }

        test("ff4k respects autoCreate parameter") {
            // When
            val ff4k = ff4k(autoCreate = true) { }

            // Then
            ff4k.check(FEATURE_NON_EXISTENT).shouldBeFalse()
            ff4k.hasFeature(FEATURE_NON_EXISTENT).shouldBeTrue()
        }

        test("ff4k creates complete configuration with all options") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy()
            val preBuiltFeature = Feature(FEATURE_LEGACY, isEnabled = false)
            val preBuiltProperty = PropertyInt(PROPERTY_PORT, VALUE_PORT)

            // When
            val ff4k = ff4k {
                feature(preBuiltFeature)
                property(preBuiltProperty)

                features {
                    feature(FEATURE_DARK_MODE) {
                        isEnabled = true
                        description = DESCRIPTION_DARK_MODE
                        group = GROUP_UI
                        flippingStrategy = strategy
                        permissions(PERMISSION_ADMIN, PERMISSION_USER)
                        property(PROPERTY_THEME) {
                            value = VALUE_THEME
                        }
                    }
                    feature(FEATURE_BETA) {
                        isEnabled = false
                        group = GROUP_EXPERIMENTAL
                    }
                }

                properties {
                    property(PROPERTY_MAX_RETRIES) {
                        value = VALUE_MAX_RETRIES
                        description = DESCRIPTION_MAX_RETRIES
                        readOnly = true
                    }
                    property(PROPERTY_API_URL) {
                        value = VALUE_API_URL
                    }
                }
            }

            // Then
            ff4k.features().size shouldBe 3
            ff4k.hasFeature(FEATURE_LEGACY).shouldBeTrue()
            ff4k.hasFeature(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.hasFeature(FEATURE_BETA).shouldBeTrue()

            val darkModeFeature = ff4k.feature(FEATURE_DARK_MODE)
            darkModeFeature.description shouldBe DESCRIPTION_DARK_MODE
            darkModeFeature.group shouldBe GROUP_UI
            darkModeFeature.flippingStrategy shouldBe strategy
            darkModeFeature.permissions shouldBe setOf(PERMISSION_ADMIN, PERMISSION_USER)
            darkModeFeature.customProperties shouldContainKey PROPERTY_THEME

            ff4k.properties().size shouldBe 3
            ff4k.hasProperty(PROPERTY_PORT).shouldBeTrue()
            ff4k.hasProperty(PROPERTY_MAX_RETRIES).shouldBeTrue()
            ff4k.hasProperty(PROPERTY_API_URL).shouldBeTrue()

            val maxRetriesProperty = ff4k.property<Int>(PROPERTY_MAX_RETRIES)
            maxRetriesProperty.shouldNotBeNull().let {
                it.description shouldBe DESCRIPTION_MAX_RETRIES
                it.readOnly.shouldBeTrue()
            }
        }

        test("ff4k features block can add pre-built features") {
            // Given
            val feature1 = Feature(FEATURE_DARK_MODE, isEnabled = true)
            val feature2 = Feature(FEATURE_BETA, isEnabled = false)

            // When
            val ff4k = ff4k {
                features {
                    feature(feature1)
                    feature(feature2)
                }
            }

            // Then
            ff4k.features().size shouldBe 2
            ff4k.check(FEATURE_DARK_MODE).shouldBeTrue()
            ff4k.check(FEATURE_BETA).shouldBeFalse()
        }

        test("ff4k properties block can add pre-built properties") {
            // Given
            val property1 = PropertyString(PROPERTY_API_URL, VALUE_API_URL)
            val property2 = PropertyInt(PROPERTY_MAX_RETRIES, VALUE_MAX_RETRIES)

            // When
            val ff4k = ff4k {
                properties {
                    property(property1)
                    property(property2)
                }
            }

            // Then
            ff4k.properties().size shouldBe 2
            ff4k.property<String>(PROPERTY_API_URL)?.value shouldBe VALUE_API_URL
            ff4k.property<Int>(PROPERTY_MAX_RETRIES)?.value shouldBe VALUE_MAX_RETRIES
        }
    }) {

    private companion object {
        private const val FEATURE_DARK_MODE = "dark-mode"
        private const val FEATURE_BETA = "beta-program"
        private const val FEATURE_PREMIUM = "premium-tier"
        private const val FEATURE_LEGACY = "legacy-feature"
        private const val FEATURE_NON_EXISTENT = "non-existent"

        private const val DESCRIPTION_DARK_MODE = "Enable dark mode theme"
        private const val DESCRIPTION_MAX_RETRIES = "Maximum retry attempts"

        private const val GROUP_UI = "ui"
        private const val GROUP_EXPERIMENTAL = "experimental"

        private const val PERMISSION_ADMIN = "ROLE_ADMIN"
        private const val PERMISSION_USER = "ROLE_USER"

        private const val PROPERTY_API_URL = "api.base.url"
        private const val PROPERTY_MAX_RETRIES = "max-retries"
        private const val PROPERTY_TIMEOUT_MS = "timeout-ms"
        private const val PROPERTY_PORT = "server.port"
        private const val PROPERTY_THEME = "theme"

        private const val VALUE_API_URL = "https://api.example.com"
        private const val VALUE_MAX_RETRIES = 3
        private const val VALUE_TIMEOUT_MS = 5000L
        private const val VALUE_PORT = 8080
        private const val VALUE_THEME = "dark"
    }
}
