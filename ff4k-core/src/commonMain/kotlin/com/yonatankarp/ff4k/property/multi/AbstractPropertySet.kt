package com.yonatankarp.ff4k.property.multi

/**
 * SuperClass for property as lists.
 *
 * @param <T> current type
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractPropertySet<T, S : MutableSet<T>>(
    name: String,
    value: S,
    description: String? = null,
    fixedValues: MutableSet<T> = mutableSetOf<T>(),
    readOnly: Boolean = false,
) : AbstractPropertyMultiValued<T, S>(name, value, description, fixedValues as Set<S>, readOnly),
    MutableSet<T>
