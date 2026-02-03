package com.yonatankarp.ff4k.dsl.strategy

import com.yonatankarp.ff4k.dsl.feature.feature
import com.yonatankarp.ff4k.strategy.AllowListStrategy
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.DenyListStrategy
import com.yonatankarp.ff4k.strategy.PonderationStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.UserPonderationStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

internal class StrategyDslTest :
    FunSpec({

        test("ponderationStrategy with Double sets strategy") {
            val feature = feature("test") {
                ponderationStrategy(0.75)
            }

            feature.flippingStrategy.shouldBeInstanceOf<PonderationStrategy>()
            feature.flippingStrategy.weight shouldBe 0.75
        }

        test("ponderationStrategy with Int sets strategy") {
            val feature = feature("test") {
                ponderationStrategy(50)
            }

            feature.flippingStrategy.shouldBeInstanceOf<PonderationStrategy>()
            feature.flippingStrategy.weight shouldBe 0.5
        }

        test("userPonderationStrategy with Double sets strategy") {
            val feature = feature("test") {
                userPonderationStrategy(0.75)
            }

            feature.flippingStrategy.shouldBeInstanceOf<UserPonderationStrategy>()
            feature.flippingStrategy.weight shouldBe 0.75
        }

        test("userPonderationStrategy with Int sets strategy") {
            val feature = feature("test") {
                userPonderationStrategy(50)
            }

            feature.flippingStrategy.shouldBeInstanceOf<UserPonderationStrategy>()
            feature.flippingStrategy.weight shouldBe 0.5
        }

        context("allowListStrategy") {
            test("sets AllowListStrategy with unary plus operator") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        +"user-2"
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                feature.flippingStrategy.allowedList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
            }

            test("sets AllowListStrategy with add function") {
                val feature = feature("test") {
                    allowListStrategy {
                        add("user-1")
                        add("user-2")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                feature.flippingStrategy.allowedList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
            }

            test("sets AllowListStrategy with addAll function") {
                val feature = feature("test") {
                    allowListStrategy {
                        addAll("user-1", "user-2", "user-3")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                feature.flippingStrategy.allowedList shouldContainExactlyInAnyOrder listOf("user-1", "user-2", "user-3")
            }

            test("sets AllowListStrategy with mixed methods") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        add("user-2")
                        addAll("user-3", "user-4")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                feature.flippingStrategy.allowedList shouldContainExactlyInAnyOrder listOf("user-1", "user-2", "user-3", "user-4")
            }

            test("deduplicates entries") {
                val feature = feature("test") {
                    allowListStrategy {
                        +"user-1"
                        +"user-1"
                        add("user-1")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<AllowListStrategy>()
                feature.flippingStrategy.allowedList shouldBe setOf("user-1")
            }
        }

        context("denyListStrategy") {
            test("sets DenyListStrategy with unary plus operator") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        +"user-2"
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                feature.flippingStrategy.denyList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
            }

            test("sets DenyListStrategy with add function") {
                val feature = feature("test") {
                    denyListStrategy {
                        add("user-1")
                        add("user-2")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                feature.flippingStrategy.denyList shouldContainExactlyInAnyOrder listOf("user-1", "user-2")
            }

            test("sets DenyListStrategy with addAll function") {
                val feature = feature("test") {
                    denyListStrategy {
                        addAll("user-1", "user-2", "user-3")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                feature.flippingStrategy.denyList shouldContainExactlyInAnyOrder listOf("user-1", "user-2", "user-3")
            }

            test("sets DenyListStrategy with mixed methods") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        add("user-2")
                        addAll("user-3", "user-4")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                feature.flippingStrategy.denyList shouldContainExactlyInAnyOrder listOf("user-1", "user-2", "user-3", "user-4")
            }

            test("deduplicates entries") {
                val feature = feature("test") {
                    denyListStrategy {
                        +"user-1"
                        +"user-1"
                        add("user-1")
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<DenyListStrategy>()
                feature.flippingStrategy.denyList shouldBe setOf("user-1")
            }
        }

        context("releaseDateStrategy") {
            test("sets ReleaseDateStrategy with Instant") {
                val date = Instant.parse("2025-01-01T00:00:00Z")
                val feature = feature("test") {
                    releaseDateStrategy(date)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe date
            }

            test("sets ReleaseDateStrategy with ISO string") {
                val dateString = "2025-01-01T00:00:00Z"
                val feature = feature("test") {
                    releaseDateStrategy(dateString)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe Instant.parse(dateString)
            }

            test("sets ReleaseDateStrategy with LocalDateTime") {
                val dateTime = LocalDateTime(2025, 1, 1, 0, 0)
                val timezone = TimeZone.UTC
                val feature = feature("test") {
                    releaseDateStrategy(dateTime, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<ReleaseDateStrategy>()
                feature.flippingStrategy.releaseDate shouldBe dateTime.toInstant(timezone)
            }
        }

        context("dateRangeStrategy") {
            test("sets DateRangeStrategy with Instant") {
                val start = Instant.parse("2025-01-01T00:00:00Z")
                val end = Instant.parse("2025-01-02T00:00:00Z")
                val feature = feature("test") {
                    dateRangeStrategy(start, end)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe start
                strategy.endDate shouldBe end
            }

            test("sets DateRangeStrategy with ISO string") {
                val start = "2025-01-01T00:00:00Z"
                val end = "2025-01-02T00:00:00Z"
                val feature = feature("test") {
                    dateRangeStrategy(start, end)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe Instant.parse(start)
                strategy.endDate shouldBe Instant.parse(end)
            }

            test("sets DateRangeStrategy with LocalDateTime") {
                val start = LocalDateTime(2025, 1, 1, 0, 0)
                val end = LocalDateTime(2025, 1, 2, 0, 0)
                val timezone = TimeZone.UTC
                val feature = feature("test") {
                    dateRangeStrategy(start, end, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DateRangeStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startDate shouldBe start.toInstant(timezone)
                strategy.endDate shouldBe end.toInstant(timezone)
            }
        }

        context("dailyHoursStrategy") {
            test("sets DailyHoursStrategy") {
                val startHour = 9
                val endHour = 17
                val timezone = TimeZone.of("Europe/Paris")

                val feature = feature("test") {
                    dailyHoursStrategy(startHour, endHour, timezone)
                }

                feature.flippingStrategy.shouldBeInstanceOf<DailyHoursStrategy>()
                val strategy = feature.flippingStrategy
                strategy.startHour shouldBe startHour
                strategy.endHour shouldBe endHour
                strategy.timezone shouldBe timezone
            }
        }

        context("weekdayStrategy") {
            test("sets WeekdayStrategy with unary plus operator") {
                val timezone = TimeZone.of("Asia/Tokyo")
                val feature = feature("test") {
                    weekdayStrategy(timezone) {
                        +DayOfWeek.MONDAY
                        +DayOfWeek.FRIDAY
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                val strategy = feature.flippingStrategy
                strategy.allowedDays shouldContainExactlyInAnyOrder setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
                strategy.timezone shouldBe timezone
            }

            test("sets WeekdayStrategy with add function") {
                val feature = feature("test") {
                    weekdayStrategy {
                        add(DayOfWeek.TUESDAY)
                        add(DayOfWeek.THURSDAY)
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            }

            test("sets WeekdayStrategy with addAll function") {
                val feature = feature("test") {
                    weekdayStrategy {
                        addAll(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            }

            test("sets WeekdayStrategy with weekdays() helper") {
                val feature = feature("test") {
                    weekdayStrategy {
                        weekdays()
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                )
            }

            test("sets WeekdayStrategy with weekends() helper") {
                val feature = feature("test") {
                    weekdayStrategy {
                        weekends()
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            }

            test("sets WeekdayStrategy with allDays() helper") {
                val feature = feature("test") {
                    weekdayStrategy {
                        allDays()
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder DayOfWeek.entries.toSet()
            }

            test("sets WeekdayStrategy with mixed methods") {
                val feature = feature("test") {
                    weekdayStrategy {
                        +DayOfWeek.MONDAY
                        weekends()
                    }
                }

                feature.flippingStrategy.shouldBeInstanceOf<WeekdayStrategy>()
                feature.flippingStrategy.allowedDays shouldContainExactlyInAnyOrder setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY,
                )
            }
        }
    })
