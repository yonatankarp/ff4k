package com.yonatankarp.ff4k.dsl.core

/**
 * Marks the boundary of the FF4K Kotlin DSL.
 *
 * This annotation is used on FF4K DSL builder classes to prevent
 * accidental access to outer DSL receivers when using nested DSL blocks.
 *
 * It enforces correct scoping rules at compile time and ensures
 * safe and readable DSL usage.
 *
 * @see DslMarker
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class FF4kDsl
