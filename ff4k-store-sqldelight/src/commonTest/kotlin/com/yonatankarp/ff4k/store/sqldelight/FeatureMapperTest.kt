package com.yonatankarp.ff4k.store.sqldelight.mapper

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

internal class FeatureMapperTest :
    FunSpec({

        val mapper = FeatureMapper()

        context("encodeEnabled") {
            test("should encode true as 1L") {
                // Given
                val enabled = true

                // When
                val result = mapper.encodeEnabled(enabled)

                // Then
                result shouldBe 1L
            }

            test("should encode false as 0L") {
                // Given
                val enabled = false

                // When
                val result = mapper.encodeEnabled(enabled)

                // Then
                result shouldBe 0L
            }
        }

        context("encodePermissions") {
            test("should encode empty set as empty JSON array") {
                // Given
                val permissions = emptySet<String>()

                // When
                val result = mapper.encodePermissions(permissions)

                // Then
                result shouldBe "[]"
            }

            test("should encode single permission") {
                // Given
                val permissions = setOf("ADMIN")

                // When
                val result = mapper.encodePermissions(permissions)

                // Then
                result shouldContain "ADMIN"
            }

            test("should encode multiple permissions") {
                // Given
                val permissions = setOf("ADMIN", "USER", "GUEST")

                // When
                val result = mapper.encodePermissions(permissions)

                // Then
                result shouldContain "ADMIN"
                result shouldContain "USER"
                result shouldContain "GUEST"
            }
        }

        context("encodeStrategy") {
            test("should encode null strategy as null") {
                // Given
                val strategy = null

                // When
                val result = mapper.encodeStrategy(strategy)

                // Then
                result.shouldBeNull()
            }

            test("should encode AlwaysTrueFlippingStrategy with type discriminator") {
                // Given
                val strategy = AlwaysTrueFlippingStrategy

                // When
                val result = mapper.encodeStrategy(strategy)

                // Then
                result shouldContain """"type""""
                result shouldContain "alwaysTrue"
            }

            test("should encode PonderationStrategy with parameters") {
                // Given
                val strategy = PonderationStrategy(weight = 0.3)

                // When
                val result = mapper.encodeStrategy(strategy)

                // Then
                result shouldContain """"type""""
                result shouldContain "ponderation"
                result shouldContain "0.3"
            }
        }

        context("encodeProperties") {
            test("should encode empty map as empty JSON object") {
                // Given
                val properties = emptyMap<String, PropertyInt>()

                // When
                val result = mapper.encodeProperties(properties)

                // Then
                result shouldBe "{}"
            }

            test("should encode single property with type discriminator") {
                // Given
                val properties = mapOf("count" to PropertyInt(name = "count", value = 42))

                // When
                val result = mapper.encodeProperties(properties)

                // Then
                result shouldContain """"count""""
                result shouldContain """"type""""
                result shouldContain "int"
                result shouldContain "42"
            }

            test("should encode multiple properties") {
                // Given
                val properties = mapOf(
                    "count" to PropertyInt(name = "count", value = 42),
                    "name" to PropertyString(name = "name", value = "test"),
                )

                // When
                val result = mapper.encodeProperties(properties)

                // Then
                result shouldContain """"count""""
                result shouldContain """"name""""
                result shouldContain "int"
                result shouldContain "string"
            }
        }

        context("toDomain") {
            test("should convert row with minimal fields") {
                // Given
                val row = Features(
                    uid = "test-feature",
                    enabled = 0L,
                    group_name = null,
                    description = null,
                    permissions = "[]",
                    flipping_strategy = null,
                    custom_properties = "{}",
                )

                // When
                val feature = mapper.toDomain(row)

                // Then
                feature.uid shouldBe "test-feature"
                feature.isEnabled.shouldBeFalse()
                feature.group.shouldBeNull()
                feature.description.shouldBeNull()
                feature.permissions.shouldBeEmpty()
                feature.flippingStrategy.shouldBeNull()
                feature.customProperties.shouldBeEmpty()
            }

            test("should convert row with enabled = 1L to isEnabled = true") {
                // Given
                val row = Features(
                    uid = "test-feature",
                    enabled = 1L,
                    group_name = null,
                    description = null,
                    permissions = "[]",
                    flipping_strategy = null,
                    custom_properties = "{}",
                )

                // When
                val feature = mapper.toDomain(row)

                // Then
                feature.isEnabled.shouldBeTrue()
            }

            test("should convert row with all fields populated") {
                // Given
                val row = Features(
                    uid = "full-feature",
                    enabled = 1L,
                    group_name = "test-group",
                    description = "A test feature",
                    permissions = """["ADMIN", "USER"]""",
                    flipping_strategy = """{"type":"alwaysTrue"}""",
                    custom_properties = """{"count":{"type":"int","name":"count","value":42,"fixedValues":[],"readOnly":false}}""",
                )

                // When
                val feature = mapper.toDomain(row)

                // Then
                feature.uid shouldBe "full-feature"
                feature.isEnabled.shouldBeTrue()
                feature.group shouldBe "test-group"
                feature.description shouldBe "A test feature"
                feature.permissions shouldContainExactlyInAnyOrder listOf("ADMIN", "USER")
                feature.flippingStrategy.shouldBeInstanceOf<AlwaysTrueFlippingStrategy>()
                feature.customProperties.size shouldBe 1
                feature.customProperties["count"].shouldBeInstanceOf<PropertyInt>()
                (feature.customProperties["count"] as PropertyInt).value shouldBe 42
            }

            test("should handle PonderationStrategy with weight") {
                // Given
                val row = Features(
                    uid = "test-feature",
                    enabled = 0L,
                    group_name = null,
                    description = null,
                    permissions = "[]",
                    flipping_strategy = """{"type":"ponderation","weight":0.75}""",
                    custom_properties = "{}",
                )

                // When
                val feature = mapper.toDomain(row)

                // Then
                val strategy = feature.flippingStrategy
                strategy.shouldBeInstanceOf<PonderationStrategy>()
                strategy.weight shouldBe 0.75
            }
        }

        context("round-trip serialization") {
            test("should preserve feature data through encode/decode cycle") {
                // Given
                val originalFeature = Feature(
                    uid = "round-trip-test",
                    isEnabled = true,
                    group = "test-group",
                    description = "Test description",
                    permissions = setOf("ADMIN", "USER"),
                    flippingStrategy = PonderationStrategy(weight = 0.6),
                    customProperties = mapOf(
                        "timeout" to PropertyInt(name = "timeout", value = 30),
                        "endpoint" to PropertyString(name = "endpoint", value = "https://api.example.com"),
                    ),
                )
                val row = Features(
                    uid = originalFeature.uid,
                    enabled = mapper.encodeEnabled(originalFeature.isEnabled),
                    group_name = originalFeature.group,
                    description = originalFeature.description,
                    permissions = mapper.encodePermissions(originalFeature.permissions),
                    flipping_strategy = mapper.encodeStrategy(originalFeature.flippingStrategy),
                    custom_properties = mapper.encodeProperties(originalFeature.customProperties),
                )

                // When
                val decoded = mapper.toDomain(row)

                // Then
                decoded.uid shouldBe originalFeature.uid
                decoded.isEnabled shouldBe originalFeature.isEnabled
                decoded.group shouldBe originalFeature.group
                decoded.description shouldBe originalFeature.description
                decoded.permissions shouldBe originalFeature.permissions
                decoded.flippingStrategy.shouldBeInstanceOf<PonderationStrategy>()
                (decoded.flippingStrategy as PonderationStrategy).weight shouldBe 0.6
                decoded.customProperties.size shouldBe 2
                (decoded.customProperties["timeout"] as PropertyInt).value shouldBe 30
                (decoded.customProperties["endpoint"] as PropertyString).value shouldBe "https://api.example.com"
            }
        }
    })
