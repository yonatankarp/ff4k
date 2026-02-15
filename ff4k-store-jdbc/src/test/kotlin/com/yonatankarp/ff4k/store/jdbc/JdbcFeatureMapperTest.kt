package com.yonatankarp.ff4k.store.jdbc

import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class JdbcFeatureMapperTest : FunSpec({

    val mapper = JdbcFeatureMapper()

    context("encodeEnabled") {
        test("returns 1L when enabled is true") {
            // Given
            val enabled = true

            // When
            val result = mapper.encodeEnabled(enabled)

            // Then
            result shouldBe 1L
        }

        test("returns 0L when enabled is false") {
            // Given
            val enabled = false

            // When
            val result = mapper.encodeEnabled(enabled)

            // Then
            result shouldBe 0L
        }
    }

    context("encodePermissions") {
        test("encodes empty permissions set") {
            // Given
            val permissions = emptySet<String>()

            // When
            val result = mapper.encodePermissions(permissions)

            // Then
            result shouldBe "[]"
        }

        test("encodes single permission") {
            // Given
            val permissions = setOf("ADMIN")

            // When
            val result = mapper.encodePermissions(permissions)

            // Then
            result shouldBe """["ADMIN"]"""
        }

        test("encodes multiple permissions") {
            // Given
            val permissions = setOf("ADMIN", "USER")

            // When
            val encoded = mapper.encodePermissions(permissions)
            val row = createRow(permissions = encoded)
            val feature = mapper.toDomain(row)

            // Then
            feature.permissions shouldContainExactlyInAnyOrder listOf("ADMIN", "USER")
        }
    }

    context("encodeStrategy") {
        test("returns null when strategy is null") {
            // Given
            val strategy = null

            // When
            val result = mapper.encodeStrategy(strategy)

            // Then
            result.shouldBeNull()
        }

        test("encodes AlwaysTrueFlippingStrategy") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy

            // When
            val result = mapper.encodeStrategy(strategy)

            // Then
            result shouldBe """{"type":"alwaysTrue"}"""
        }
    }

    context("encodeProperties") {
        test("encodes empty properties map") {
            // Given
            val properties = emptyMap<String, PropertyInt>()

            // When
            val result = mapper.encodeProperties(properties)

            // Then
            result shouldBe "{}"
        }

        test("encodes single property") {
            // Given
            val property = PropertyInt(name = "maxRetries", value = 3)
            val properties = mapOf("maxRetries" to property)

            // When
            val encoded = mapper.encodeProperties(properties)
            val row = createRow(customProperties = encoded)
            val feature = mapper.toDomain(row)

            // Then
            feature.customProperties shouldContainKey "maxRetries"
            feature.customProperties["maxRetries"].shouldBeInstanceOf<PropertyInt>()
            (feature.customProperties["maxRetries"] as PropertyInt).value shouldBe 3
        }

        test("encodes multiple properties") {
            // Given
            val intProperty = PropertyInt(name = "maxRetries", value = 3)
            val stringProperty = PropertyString(name = "region", value = "US")
            val properties = mapOf(
                "maxRetries" to intProperty,
                "region" to stringProperty,
            )

            // When
            val encoded = mapper.encodeProperties(properties)
            val row = createRow(customProperties = encoded)
            val feature = mapper.toDomain(row)

            // Then
            feature.customProperties.size shouldBe 2
            (feature.customProperties["maxRetries"] as PropertyInt).value shouldBe 3
            (feature.customProperties["region"] as PropertyString).value shouldBe "US"
        }
    }

    context("toDomain") {
        test("converts minimal row to feature") {
            // Given
            val row = createRow()

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

        test("converts row with enabled=1 to feature with isEnabled=true") {
            // Given
            val row = createRow(enabled = 1L)

            // When
            val feature = mapper.toDomain(row)

            // Then
            feature.isEnabled.shouldBeTrue()
        }

        test("converts row with non-zero enabled value to feature with isEnabled=true") {
            // Given
            val row = createRow(enabled = 42L)

            // When
            val feature = mapper.toDomain(row)

            // Then
            feature.isEnabled.shouldBeTrue()
        }

        test("converts row with all fields to feature") {
            // Given
            val strategy = AlwaysTrueFlippingStrategy
            val property = PropertyInt(name = "count", value = 5)
            val row = createRow(
                uid = "full-feature",
                enabled = 1L,
                groupName = "test-group",
                description = "A test feature",
                permissions = """["ADMIN","USER"]""",
                flippingStrategy = mapper.encodeStrategy(strategy),
                customProperties = mapper.encodeProperties(mapOf("count" to property)),
            )

            // When
            val feature = mapper.toDomain(row)

            // Then
            feature.uid shouldBe "full-feature"
            feature.isEnabled.shouldBeTrue()
            feature.group shouldBe "test-group"
            feature.description shouldBe "A test feature"
            feature.permissions shouldContainExactlyInAnyOrder listOf("ADMIN", "USER")
            feature.flippingStrategy shouldBe AlwaysTrueFlippingStrategy
            (feature.customProperties["count"] as PropertyInt).value shouldBe 5
        }
    }

    context("round-trip encoding") {
        test("Feature survives encode-decode round trip") {
            // Given
            val original = Feature(
                uid = "round-trip-test",
                isEnabled = true,
                group = "test-group",
                description = "Round trip test",
                permissions = setOf("ADMIN", "USER"),
                flippingStrategy = AlwaysTrueFlippingStrategy,
                customProperties = mapOf(
                    "maxRetries" to PropertyInt(name = "maxRetries", value = 3),
                    "region" to PropertyString(name = "region", value = "EU"),
                ),
            )

            // When
            val row = FeatureRow(
                uid = original.uid,
                enabled = mapper.encodeEnabled(original.isEnabled),
                groupName = original.group,
                description = original.description,
                permissions = mapper.encodePermissions(original.permissions),
                flippingStrategy = mapper.encodeStrategy(original.flippingStrategy),
                customProperties = mapper.encodeProperties(original.customProperties),
                version = 1L,
            )
            val restored = mapper.toDomain(row)

            // Then
            restored.uid shouldBe original.uid
            restored.isEnabled shouldBe original.isEnabled
            restored.group shouldBe original.group
            restored.description shouldBe original.description
            restored.permissions shouldBe original.permissions
            restored.flippingStrategy shouldBe original.flippingStrategy
            restored.customProperties.size shouldBe original.customProperties.size
            (restored.customProperties["maxRetries"] as PropertyInt).value shouldBe 3
            (restored.customProperties["region"] as PropertyString).value shouldBe "EU"
        }
    }
})

private fun createRow(
    uid: String = "test-feature",
    enabled: Long = 0L,
    groupName: String? = null,
    description: String? = null,
    permissions: String = "[]",
    flippingStrategy: String? = null,
    customProperties: String = "{}",
    version: Long = 1L,
) = FeatureRow(
    uid = uid,
    enabled = enabled,
    groupName = groupName,
    description = description,
    permissions = permissions,
    flippingStrategy = flippingStrategy,
    customProperties = customProperties,
    version = version,
)
