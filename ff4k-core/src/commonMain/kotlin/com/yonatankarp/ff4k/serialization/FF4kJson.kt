package com.yonatankarp.ff4k.serialization

import com.yonatankarp.ff4k.FlippingStrategy
import com.yonatankarp.ff4k.strategy.AndStrategy
import com.yonatankarp.ff4k.strategy.ContextFilterStrategy
import com.yonatankarp.ff4k.strategy.DailyHoursStrategy
import com.yonatankarp.ff4k.strategy.DateRangeStrategy
import com.yonatankarp.ff4k.strategy.NotStrategy
import com.yonatankarp.ff4k.strategy.OrStrategy
import com.yonatankarp.ff4k.strategy.PercentageStrategy
import com.yonatankarp.ff4k.strategy.ReleaseDateStrategy
import com.yonatankarp.ff4k.strategy.UserPercentageStrategy
import com.yonatankarp.ff4k.strategy.WeekdayStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Registers the built-in strategies. Combine with your own module (`ff4kSerializersModule + SerializersModule { ... }`)
 * to add custom strategies (`polymorphic(FlippingStrategy::class)`) or property value types (`polymorphic(Any::class)`).
 */
val ff4kSerializersModule = SerializersModule {
    polymorphic(FlippingStrategy::class) {
        subclass(AndStrategy::class)
        subclass(OrStrategy::class)
        subclass(NotStrategy::class)
        subclass(ContextFilterStrategy::class)
        subclass(PercentageStrategy::class)
        subclass(UserPercentageStrategy::class)
        subclass(ReleaseDateStrategy::class)
        subclass(DateRangeStrategy::class)
        subclass(DailyHoursStrategy::class)
        subclass(WeekdayStrategy::class)
    }
}

val ff4kJson = Json {
    serializersModule = ff4kSerializersModule
    ignoreUnknownKeys = true
    prettyPrint = true
}
