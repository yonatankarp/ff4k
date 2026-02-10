package com.yonatankarp.ff4k.store

import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyDouble
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.store.sqldelight.sqlite.Properties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

internal class SqlitePropertyMapperTest :
    FunSpec({

        val mapper = SqlitePropertyMapper()

        context("encodeReadOnly") {
            test("should encode true as 1L") {
                // Given
                val readOnly = true

                // When
                val result = mapper.encodeReadOnly(readOnly)

                // Then
                result shouldBe 1L
            }

            test("should encode false as 0L") {
                // Given
                val readOnly = false

                // When
                val result = mapper.encodeReadOnly(readOnly)

                // Then
                result shouldBe 0L
            }
        }

        context("encodeType") {
            test("should encode PropertyString as 'string'") {
                // Given
                val property = PropertyString(name = "test", value = "hello")

                // When
                val result = mapper.encodeType(property)

                // Then
                result shouldBe "string"
            }

            test("should encode PropertyInt as 'int'") {
                // Given
                val property = PropertyInt(name = "test", value = 42)

                // When
                val result = mapper.encodeType(property)

                // Then
                result shouldBe "int"
            }

            test("should encode PropertyBoolean as 'boolean'") {
                // Given
                val property = PropertyBoolean(name = "test", value = true)

                // When
                val result = mapper.encodeType(property)

                // Then
                result shouldBe "boolean"
            }

            test("should encode PropertyDouble as 'double'") {
                // Given
                val property = PropertyDouble(name = "test", value = 3.14)

                // When
                val result = mapper.encodeType(property)

                // Then
                result shouldBe "double"
            }
        }

        context("encodeData") {
            test("should encode PropertyString with type discriminator") {
                // Given
                val property = PropertyString(name = "greeting", value = "hello")

                // When
                val result = mapper.encodeData(property)

                // Then
                result shouldContain """"type":"string""""
                result shouldContain """"name":"greeting""""
                result shouldContain """"value":"hello""""
            }

            test("should encode PropertyInt with all fields") {
                // Given
                val property = PropertyInt(
                    name = "count",
                    value = 42,
                    description = "A counter",
                    readOnly = true,
                )

                // When
                val result = mapper.encodeData(property)

                // Then
                result shouldContain """"type":"int""""
                result shouldContain """"name":"count""""
                result shouldContain """"value":42"""
                result shouldContain """"description":"A counter""""
                result shouldContain """"readOnly":true"""
            }

            test("should encode PropertyString with fixed values") {
                // Given
                val property = PropertyString(
                    name = "env",
                    value = "prod",
                    fixedValues = setOf("dev", "staging", "prod"),
                )

                // When
                val result = mapper.encodeData(property)

                // Then
                result shouldContain """"fixedValues":"""
                result shouldContain "dev"
                result shouldContain "staging"
                result shouldContain "prod"
            }
        }

        context("toDomain") {
            test("should convert row with PropertyString") {
                // Given
                val row = Properties(
                    name = "greeting",
                    type = "string",
                    description = null,
                    read_only = 0L,
                    data_ = """{"type":"string","name":"greeting","value":"hello","fixedValues":[],"readOnly":false}""",
                    version = 1L,
                )

                // When
                val property = mapper.toDomain(row)

                // Then
                property.shouldBeInstanceOf<PropertyString>()
                property.name shouldBe "greeting"
                property.value shouldBe "hello"
                property.description shouldBe null
                property.readOnly shouldBe false
                property.fixedValues shouldBe emptySet()
            }

            test("should convert row with PropertyInt") {
                // Given
                val row = Properties(
                    name = "count",
                    type = "int",
                    description = "A counter",
                    read_only = 1L,
                    data_ = """{"type":"int","name":"count","value":42,"description":"A counter","fixedValues":[],"readOnly":true}""",
                    version = 1L,
                )

                // When
                val property = mapper.toDomain(row)

                // Then
                property.shouldBeInstanceOf<PropertyInt>()
                property.name shouldBe "count"
                property.value shouldBe 42
                property.description shouldBe "A counter"
                property.readOnly shouldBe true
            }

            test("should convert row with PropertyBoolean") {
                // Given
                val row = Properties(
                    name = "enabled",
                    type = "boolean",
                    description = null,
                    read_only = 0L,
                    data_ = """{"type":"boolean","name":"enabled","value":true,"fixedValues":[],"readOnly":false}""",
                    version = 1L,
                )

                // When
                val property = mapper.toDomain(row)

                // Then
                property.shouldBeInstanceOf<PropertyBoolean>()
                property.name shouldBe "enabled"
                property.value shouldBe true
            }

            test("should convert row with fixed values") {
                // Given
                val row = Properties(
                    name = "env",
                    type = "string",
                    description = null,
                    read_only = 0L,
                    data_ = """{"type":"string","name":"env","value":"prod","fixedValues":["dev","staging","prod"],"readOnly":false}""",
                    version = 1L,
                )

                // When
                val property = mapper.toDomain(row)

                // Then
                property.shouldBeInstanceOf<PropertyString>()
                property.fixedValues shouldBe setOf("dev", "staging", "prod")
            }
        }

        context("round-trip serialization") {
            test("should preserve PropertyString data through encode/decode cycle") {
                // Given
                val originalProperty = PropertyString(
                    name = "endpoint",
                    value = "https://api.example.com",
                    description = "API endpoint URL",
                    readOnly = false,
                )
                val row = Properties(
                    name = originalProperty.name,
                    type = mapper.encodeType(originalProperty),
                    description = originalProperty.description,
                    read_only = mapper.encodeReadOnly(originalProperty.readOnly),
                    data_ = mapper.encodeData(originalProperty),
                    version = 1L,
                )

                // When
                val decoded = mapper.toDomain(row)

                // Then
                decoded.shouldBeInstanceOf<PropertyString>()
                decoded.name shouldBe originalProperty.name
                decoded.value shouldBe originalProperty.value
                decoded.description shouldBe originalProperty.description
                decoded.readOnly shouldBe originalProperty.readOnly
                decoded.fixedValues shouldBe originalProperty.fixedValues
            }

            test("should preserve PropertyInt with fixed values through encode/decode cycle") {
                // Given
                val originalProperty = PropertyInt(
                    name = "priority",
                    value = 2,
                    description = "Task priority",
                    fixedValues = setOf(1, 2, 3),
                    readOnly = true,
                )
                val row = Properties(
                    name = originalProperty.name,
                    type = mapper.encodeType(originalProperty),
                    description = originalProperty.description,
                    read_only = mapper.encodeReadOnly(originalProperty.readOnly),
                    data_ = mapper.encodeData(originalProperty),
                    version = 1L,
                )

                // When
                val decoded = mapper.toDomain(row)

                // Then
                decoded.shouldBeInstanceOf<PropertyInt>()
                decoded.name shouldBe originalProperty.name
                decoded.value shouldBe originalProperty.value
                decoded.description shouldBe originalProperty.description
                decoded.readOnly shouldBe originalProperty.readOnly
                decoded.fixedValues shouldBe originalProperty.fixedValues
            }

            test("should preserve PropertyDouble through encode/decode cycle") {
                // Given
                val originalProperty = PropertyDouble(
                    name = "rate",
                    value = 0.75,
                    description = "Conversion rate",
                )
                val row = Properties(
                    name = originalProperty.name,
                    type = mapper.encodeType(originalProperty),
                    description = originalProperty.description,
                    read_only = mapper.encodeReadOnly(originalProperty.readOnly),
                    data_ = mapper.encodeData(originalProperty),
                    version = 1L,
                )

                // When
                val decoded = mapper.toDomain(row)

                // Then
                decoded.shouldBeInstanceOf<PropertyDouble>()
                decoded.name shouldBe originalProperty.name
                decoded.value shouldBe originalProperty.value
                decoded.description shouldBe originalProperty.description
            }
        }
    })
