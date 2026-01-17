package com.yonatankarp.ff4k.dsl.property

/**
 * Marker annotation for FF4K DSL scopes.
 *
 * This annotation is used to prevent accidental access to outer DSL receivers
 * when working with nested FF4K DSL blocks.
 *
 * It ensures that configuration blocks such as `fixedValues {}` can only be
 * invoked in their intended context (for example, inside a property definition),
 * providing better compile-time safety and clearer DSL usage.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class FF4kPropertyDsl
