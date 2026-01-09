package com.yonatankarp.ff4k.property.dsl

/**
 * DSL marker annotation for property builder scope.
 * Prevents scope leakage between nested DSL blocks.
 *
 * @author Yonatan Karp-Rudin
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PropertyDsl
