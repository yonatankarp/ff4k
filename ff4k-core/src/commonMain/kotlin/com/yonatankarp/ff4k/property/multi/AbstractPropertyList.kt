package com.yonatankarp.ff4k.property.multi

/**
 * SuperClass for property as lists.
 *
 * @param name Unique identifier for the property.
 * @param value Initial list of values for the property.
 * @param description Optional human-readable description.
 * @param fixedValues Set of allowed values for the property.
 * @param readOnly If true, the property cannot be modified.
 *
 * @param T current type
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class AbstractPropertyList<T>(
    name: String,
    value: MutableList<T>,
    description: String? = null,
    fixedValues: MutableSet<MutableList<T>> = mutableSetOf(),
    readOnly: Boolean = false,
) : AbstractPropertyMultiValued<T, MutableList<T>>(name, value, description, fixedValues, readOnly),
    MutableList<T> {

    /**
     * Constructor by T expression.
     *
     * @param name unique name
     * @param values values to fill the property with
     */
    constructor(name: String, vararg values: T) : this(
        name,
        values.toMutableList(),
    )

    override fun addAll(index: Int, elements: Collection<T>): Boolean = value.addAll(index, elements)

    override operator fun get(index: Int): T = value[index]

    override operator fun set(index: Int, element: T): T {
        value[index] = element
        return element
    }

    override fun add(index: Int, element: T) {
        value.add(index, element)
    }

    override fun removeAt(index: Int): T = value.removeAt(index)

    override fun indexOf(element: T): Int = value.indexOf(element)

    override fun lastIndexOf(element: T): Int = value.lastIndexOf(element)

    override fun listIterator(): MutableListIterator<T> = value.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = value.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = value.subList(fromIndex, toIndex)
}
