package com.yonatankarp.ff4k.strategy

import com.yonatankarp.ff4k.Feature
import kotlin.time.Clock
import kotlin.time.Instant

internal val feature = Feature("f", enabled = true)

internal fun fixedClock(iso: String) = object : Clock {
    override fun now(): Instant = Instant.parse(iso)
}
