package com.yonatankarp.ff4k.property.multi

import com.yonatankarp.ff4k.property.Property

/**
 * Super class to work with multivalued properties.
 *
 * @param <T> current inner type
 * @param <C> current collection type
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 */
abstract class AbstractPropertyMultiValued<T, C : MutableCollection<T>>(
    override val name: String,
    override val value: C,
    override val description: String? = null,
    override val fixedValues: Set<C> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<C>,
    MutableCollection<T> {

    /** {@inheritDoc} */
    override val size: Int get() = value.size

    /**
     * Add element to the collection.
     *
     * @param element new element
     */
    override fun add(element: T): Boolean = value.add(element)

    /** {@inheritDoc} */
    override fun addAll(elements: Collection<T>): Boolean = value.addAll(elements)

    /**
     * Add values to target collections.
     *
     * @param values target values
     */
    fun addAll(vararg values: T) {
        addAll(values.toList())
    }

    /** {@inheritDoc} */
    override fun isEmpty(): Boolean = value.isEmpty()

    /** {@inheritDoc} */
    override fun contains(element: T): Boolean = element in value

    /** {@inheritDoc} */
    override fun containsAll(elements: Collection<T>): Boolean = value.containsAll(elements)

    /** {@inheritDoc} */
    override fun remove(element: T): Boolean = value.remove(element)

    /** {@inheritDoc} */
    override fun removeAll(elements: Collection<T>): Boolean = value.removeAll(elements.toSet())

    operator fun minusAssign(element: T) {
        remove(element)
    }

    operator fun plusAssign(element: T) {
        add(element)
    }

    /** {@inheritDoc} */
    override fun retainAll(elements: Collection<T>): Boolean = value.retainAll(elements.toSet())

    /** {@inheritDoc} */
    override fun iterator(): MutableIterator<T> = value.iterator()

    /** {@inheritDoc}  */
    override fun clear() {
        value.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPropertyMultiValued<*, *>) return false

        return name == other.name &&
            value == other.value &&
            description == other.description &&
            fixedValues == other.fixedValues &&
            readOnly == other.readOnly
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + fixedValues.hashCode()
        result = 31 * result + readOnly.hashCode()
        return result
    }
}
