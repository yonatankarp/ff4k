package com.yonatankarp.ff4k.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun fixedClock(instant: Instant): Clock = object : Clock {
    override fun now(): Instant = instant
}
