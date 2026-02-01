package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.core.FlippingExecutionContext
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.serialization.FF4kJson
import com.yonatankarp.ff4k.store.InMemoryFeatureStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString

internal class CompositeFlippingStrategyTest :
    FunSpec({
        context("test AndStrategy") {
            withData(
                nameFn = { it.description },
                andStrategyCases,
            ) { (_, left, right, expected) ->
                // Given
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val andStrategy = left and right

                // Then
                andStrategy.evaluate("test", store, context) shouldBe expected
            }
        }

        test("test AndStrategy evaluates to true when no strategies supplied") {
            // Given
            val store = InMemoryFeatureStore()
            val context = FlippingExecutionContext()
            val andStrategy = AndStrategy(strategies = emptyList())

            // When
            val result = andStrategy.evaluate("test", store, context)

            // Then
            result.shouldBeTrue()
        }

        test("test OrStrategy evaluates to false when no strategies supplied") {
            // Given
            val store = InMemoryFeatureStore()
            val context = FlippingExecutionContext()
            val orStrategy = OrStrategy(strategies = emptyList())

            // When
            val result = orStrategy.evaluate("test", store, context)

            // Then
            result.shouldBeFalse()
        }

        context("test OrStrategy") {
            withData(
                nameFn = { it.description },
                orStrategyCases,
            ) { (_, left, right, expected) ->
                // Given
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val orStrategy = left or right

                // Then
                orStrategy.evaluate("test", store, context) shouldBe expected
            }
        }

        context("test NotStrategy") {
            withData(
                nameFn = { it.description },
                notStrategyCases,
            ) { (_, operand, expected) ->
                // Given
                val store = InMemoryFeatureStore()
                val context = FlippingExecutionContext()

                // When
                val notStrategy = !operand

                // Then
                notStrategy.evaluate("test", store, context) shouldBe expected
            }
        }

        context("DSL flattening for AndStrategy") {
            test("chaining 'and' creates flat list instead of nested structure") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy
                val c = AlwaysTrueFlippingStrategy

                // When
                val result = a and b and c

                // Then
                result.strategies shouldHaveSize 3
                result.strategies[0] shouldBe a
                result.strategies[1] shouldBe b
                result.strategies[2] shouldBe c
            }

            test("chaining multiple 'and' operations creates flat list") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysTrueFlippingStrategy
                val c = AlwaysTrueFlippingStrategy
                val d = AlwaysTrueFlippingStrategy
                val e = AlwaysTrueFlippingStrategy

                // When
                val result = a and b and c and d and e

                // Then
                result.strategies shouldHaveSize 5
            }

            test("'and' with non-AndStrategy creates new list") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy

                // When
                val result = a and b

                // Then
                result.strategies shouldHaveSize 2
            }
        }

        context("DSL flattening for OrStrategy") {
            test("chaining 'or' creates flat list instead of nested structure") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy
                val c = AlwaysTrueFlippingStrategy

                // When
                val result = a or b or c

                // Then
                result.strategies shouldHaveSize 3
                result.strategies[0] shouldBe a
                result.strategies[1] shouldBe b
                result.strategies[2] shouldBe c
            }

            test("chaining multiple 'or' operations creates flat list") {
                // Given
                val a = AlwaysFalseFlippingStrategy
                val b = AlwaysFalseFlippingStrategy
                val c = AlwaysFalseFlippingStrategy
                val d = AlwaysFalseFlippingStrategy
                val e = AlwaysFalseFlippingStrategy

                // When
                val result = a or b or c or d or e

                // Then
                result.strategies shouldHaveSize 5
            }

            test("'or' with non-OrStrategy creates new list") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy

                // When
                val result = a or b

                // Then
                result.strategies shouldHaveSize 2
            }
        }

        context("DSL does not flatten different strategy types") {
            test("'and' after 'or' creates nested structure") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy
                val c = AlwaysTrueFlippingStrategy

                // When
                val result = (a or b) and c

                // Then
                result.strategies shouldHaveSize 2
                result.strategies[0].shouldBeInstanceOf<OrStrategy>()
                result.strategies[1] shouldBe c
            }

            test("'or' after 'and' creates nested structure") {
                // Given
                val a = AlwaysTrueFlippingStrategy
                val b = AlwaysFalseFlippingStrategy
                val c = AlwaysTrueFlippingStrategy

                // When
                val result = (a and b) or c

                // Then
                result.strategies shouldHaveSize 2
                result.strategies[0].shouldBeInstanceOf<AndStrategy>()
                result.strategies[1] shouldBe c
            }
        }

        context("AndStrategy serialization") {
            test("serializes to JSON with nested strategies") {
                // Given
                val strategy =
                    AlwaysTrueFlippingStrategy and AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString(strategy)

                // Then
                json shouldContain "alwaysTrue"
                json shouldContain "alwaysFalse"
            }

            test("round-trip serialization") {
                // Given
                val original =
                    AlwaysTrueFlippingStrategy and AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }

            test("round-trip with nested AndStrategy") {
                // Given
                val original =
                    AndStrategy(strategies = listOf(AlwaysTrueFlippingStrategy)) and AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }
        }

        context("OrStrategy serialization") {
            test("serializes to JSON with nested strategies") {
                // Given
                val strategy =
                    AlwaysTrueFlippingStrategy or AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString(strategy)

                // Then
                json shouldContain "alwaysTrue"
                json shouldContain "alwaysFalse"
            }

            test("round-trip serialization") {
                // Given
                val original =
                    AlwaysTrueFlippingStrategy or AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }

            test("round-trip with nested OrStrategy") {
                // Given
                val original =
                    OrStrategy(strategies = listOf(AlwaysTrueFlippingStrategy)) or AlwaysFalseFlippingStrategy

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }
        }

        context("NotStrategy serialization") {
            test("serializes to JSON with nested strategy") {
                // Given
                val strategy = AlwaysTrueFlippingStrategy.not()

                // When
                val json = FF4kJson.encodeToString(strategy)

                // Then
                json shouldContain "alwaysTrue"
            }

            test("round-trip serialization") {
                // Given
                val original = AlwaysTrueFlippingStrategy.not()

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }

            test("round-trip with nested NotStrategy") {
                // Given
                val original =
                    NotStrategy(strategy = AlwaysFalseFlippingStrategy.not())

                // When
                val json = FF4kJson.encodeToString<FlippingStrategy>(original)
                val deserialized = FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe original
            }
        }

        context("complex composite strategy serialization") {
            withData(
                nameFn = { it.description },
                complexStrategyCases,
            ) { (_, strategy) ->
                // When
                val json =
                    FF4kJson.encodeToString<FlippingStrategy>(strategy)
                val deserialized =
                    FF4kJson.decodeFromString<FlippingStrategy>(json)

                // Then
                deserialized shouldBe strategy
            }
        }
    }) {
    companion object {
        private val andStrategyCases = listOf(
            CompositeFlippingStrategyTestData(
                description = "false AND false returns false",
                left = AlwaysFalseFlippingStrategy,
                right = AlwaysFalseFlippingStrategy,
                expected = false,
            ),
            CompositeFlippingStrategyTestData(
                description = "true AND false returns false",
                left = AlwaysTrueFlippingStrategy,
                right = AlwaysFalseFlippingStrategy,
                expected = false,
            ),
            CompositeFlippingStrategyTestData(
                description = "false AND true returns false",
                left = AlwaysFalseFlippingStrategy,
                right = AlwaysTrueFlippingStrategy,
                expected = false,
            ),
            CompositeFlippingStrategyTestData(
                description = "true AND true returns true",
                left = AlwaysTrueFlippingStrategy,
                right = AlwaysTrueFlippingStrategy,
                expected = true,
            ),
        )

        private val orStrategyCases = listOf(
            CompositeFlippingStrategyTestData(
                description = "false OR false returns false",
                left = AlwaysFalseFlippingStrategy,
                right = AlwaysFalseFlippingStrategy,
                expected = false,
            ),
            CompositeFlippingStrategyTestData(
                description = "true OR false returns true",
                left = AlwaysTrueFlippingStrategy,
                right = AlwaysFalseFlippingStrategy,
                expected = true,
            ),
            CompositeFlippingStrategyTestData(
                description = "false OR true returns true",
                left = AlwaysFalseFlippingStrategy,
                right = AlwaysTrueFlippingStrategy,
                expected = true,
            ),
            CompositeFlippingStrategyTestData(
                description = "true OR true returns true",
                left = AlwaysTrueFlippingStrategy,
                right = AlwaysTrueFlippingStrategy,
                expected = true,
            ),
        )

        private val notStrategyCases = listOf(
            NotStrategyTestData(
                description = "NOT false returns true",
                operand = AlwaysFalseFlippingStrategy,
                expected = true,
            ),
            NotStrategyTestData(
                description = "NOT true returns false",
                operand = AlwaysTrueFlippingStrategy,
                expected = false,
            ),
        )

        private val complexStrategyCases = listOf(
            ComplexStrategyTestData(
                description = "AND with OR children",
                strategy = AndStrategy(
                    strategies = listOf(
                        AlwaysTrueFlippingStrategy or AlwaysFalseFlippingStrategy,
                        AlwaysTrueFlippingStrategy,
                    ),
                ),
            ),
            ComplexStrategyTestData(
                description = "OR with AND children",
                strategy = OrStrategy(
                    strategies = listOf(
                        AlwaysTrueFlippingStrategy and AlwaysFalseFlippingStrategy,
                        AlwaysFalseFlippingStrategy,
                    ),
                ),
            ),
            ComplexStrategyTestData(
                description = "NOT with AND child",
                strategy = NotStrategy(
                    strategy = AlwaysTrueFlippingStrategy and AlwaysTrueFlippingStrategy,
                ),
            ),
            ComplexStrategyTestData(
                description = "NOT with OR child",
                strategy = NotStrategy(
                    strategy = AlwaysFalseFlippingStrategy or AlwaysFalseFlippingStrategy,
                ),
            ),
            ComplexStrategyTestData(
                description = "deeply nested composite",
                strategy = AndStrategy(
                    strategies = listOf(
                        AlwaysFalseFlippingStrategy.not() or (AlwaysTrueFlippingStrategy and AlwaysTrueFlippingStrategy),
                        AlwaysFalseFlippingStrategy.not(),
                    ),
                ),
            ),
        )
    }
}

private data class CompositeFlippingStrategyTestData(
    val description: String,
    val left: FlippingStrategy,
    val right: FlippingStrategy,
    val expected: Boolean,
)

private data class NotStrategyTestData(
    val description: String,
    val operand: FlippingStrategy,
    val expected: Boolean,
)

private data class ComplexStrategyTestData(
    val description: String,
    val strategy: FlippingStrategy,
)
