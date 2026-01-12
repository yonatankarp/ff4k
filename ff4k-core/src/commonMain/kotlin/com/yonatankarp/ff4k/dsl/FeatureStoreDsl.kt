package com.yonatankarp.ff4k.dsl

/**
 * DSL marker annotation for feature store builder scope.
 * Prevents scope leakage between nested DSL blocks.
 *
 * @author Yonatan Karp-Rudin
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class FeatureStoreDsl
