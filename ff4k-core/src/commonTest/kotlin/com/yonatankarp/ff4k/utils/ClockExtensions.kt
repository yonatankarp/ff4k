package com.yonatankarp.ff4k.utils

import kotlin.time.Clock
import kotlin.time.Instant

fun fixedClock(instant: Instant): Clock = object : Clock {
    override fun now(): Instant = instant
}
