package com.yonatankarp.ff4k

import com.yonatankarp.ff4k.serialization.PropertySerializer
import com.yonatankarp.ff4k.serialization.ff4kJson
import com.yonatankarp.ff4k.serialization.ff4kSerializersModule
import com.yonatankarp.ff4k.strategy.ContextFilterStrategy
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.PercentageStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.UserPercentageStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import com.yonatankarp.ff4k.strategy.and
import com.yonatankarp.ff4k.strategy.not
import com.yonatankarp.ff4k.strategy.or
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.time.Instant

@Serializable
@SerialName("version")
private data class Version(val major: Int, val minor: Int)

class FF4kConfigurationTest :
    FunSpec({
        val instant = Instant.parse("2026-01-01T00:00:00Z")

        test("round-trips every property type and strategy through JSON") {
            val configuration = FF4kConfiguration(
                features = listOf(
                    Feature("plain"),
                    Feature(
                        id = "full",
                        enabled = true,
                        description = "desc",
                        group = "g",
                        permissions = setOf("admin"),
                        strategy = (ContextFilterStrategy("region", setOf("eu")) and PercentageStrategy(50)) or
                            !UserPercentageStrategy(10) or
                            ReleaseDateStrategy(instant) or
                            DateRangeStrategy(instant, instant) or
                            DailyHoursStrategy(9, 17, TimeZone.of("Europe/Berlin")) or
                            WeekdayStrategy(setOf(DayOfWeek.MONDAY)),
                        properties = listOf(Property("limit", 3)),
                    ),
                ),
                properties = listOf(
                    Property("string", "text", "desc"),
                    Property("int", 1),
                    Property("long", 2L),
                    Property("double", 1.5),
                    Property("boolean", true),
                    Property("instant", instant),
                    Property("localDate", LocalDate(2026, 1, 1)),
                    Property("localDateTime", LocalDateTime(2026, 1, 1, 10, 30)),
                ),
            )

            FF4kConfiguration.fromJson(configuration.toJson()) shouldBe configuration
        }

        test("parses the documented JSON shape") {
            val json = """
                {
                  "features": [
                    { "id": "dark-mode", "enabled": true, "group": "ui",
                      "strategy": { "type": "contextFilter", "key": "region", "allowed": ["eu"] } }
                  ],
                  "properties": [
                    { "name": "max-retries", "type": "int", "value": 3, "description": "Retries" }
                  ]
                }
            """.trimIndent()

            val configuration = FF4kConfiguration.fromJson(json)
            configuration.features.single() shouldBe
                Feature("dark-mode", enabled = true, group = "ui", strategy = ContextFilterStrategy("region", setOf("eu")))
            configuration.properties.single() shouldBe Property("max-retries", 3, "Retries")
        }

        test("rejects unregistered property value types on encode") {
            shouldThrow<SerializationException> {
                ff4kJson.encodeToString(PropertySerializer, Property("p", Version(1, 2)))
            }.message shouldContain "Version"
        }

        test("custom property value types are registered through the serializers module") {
            val json = Json {
                serializersModule = ff4kSerializersModule + SerializersModule {
                    polymorphic(Any::class) { subclass(Version::class) }
                }
            }
            val configuration = FF4kConfiguration(properties = listOf(Property("minimum", Version(1, 2))))

            val text = json.encodeToString(configuration)
            text shouldContain "\"type\":\"version\""
            json.decodeFromString<FF4kConfiguration>(text) shouldBe configuration
        }

        test("rejects unknown property types on decode") {
            shouldThrow<SerializationException> {
                ff4kJson.decodeFromString(PropertySerializer, """{"name":"p","type":"uuid","value":"x"}""")
            }.message shouldContain "uuid"
        }

        test("blank ids and names are rejected") {
            shouldThrow<IllegalArgumentException> { Feature(" ") }
            shouldThrow<IllegalArgumentException> { Property("", 1) }
        }

        test("feature property lookup by name") {
            val feature = Feature("f", properties = listOf(Property("limit", 3)))
            feature.property("limit")?.value shouldBe 3
            feature.property("missing") shouldBe null
        }
    })
