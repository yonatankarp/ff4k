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

    init {
        require(fixedValues.isEmpty() || value in fixedValues) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }

    override val size: Int get() = value.size

    /**
     * Add element to the collection.
     *
     * @param element new element
     */
    override fun add(element: T): Boolean = value.add(element)

    override fun addAll(elements: Collection<T>): Boolean = value.addAll(elements)

    /**
     * Add values to target collections.
     *
     * @param values target values
     */
    fun addAll(vararg values: T) {
        addAll(values.toList())
    }

    override fun isEmpty(): Boolean = value.isEmpty()

    override fun contains(element: T): Boolean = element in value

    override fun containsAll(elements: Collection<T>): Boolean = value.containsAll(elements)

    override fun remove(element: T): Boolean = value.remove(element)

    override fun removeAll(elements: Collection<T>): Boolean = value.removeAll(elements.toSet())

    operator fun minusAssign(element: T) {
        remove(element)
    }

    operator fun plusAssign(element: T) {
        add(element)
    }

    override fun retainAll(elements: Collection<T>): Boolean = value.retainAll(elements.toSet())

    override fun iterator(): MutableIterator<T> = value.iterator()

    override fun clear() {
        value.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        // Delegate to appropriate equality check based on type
        // This ensures compatibility with standard Kotlin collections on all platforms
        return when {
            other is AbstractPropertyMultiValued<*, *> -> equalsProperty(other)
            value is List<*> && other is List<*> -> value == other
            value is Set<*> && other is Set<*> -> value == other
            else -> false
        }
    }

    private fun equalsProperty(other: AbstractPropertyMultiValued<*, *>): Boolean = name == other.name &&
        value == other.value &&
        description == other.description &&
        fixedValues == other.fixedValues &&
        readOnly == other.readOnly

    override fun hashCode(): Int {
        // Use collection hashCode for List/Set types to maintain equals/hashCode contract
        return when (value) {
            is List<*> -> value.hashCode()
            is Set<*> -> value.hashCode()
            else -> {
                var result = name.hashCode()
                result = 31 * result + value.hashCode()
                result = 31 * result + (description?.hashCode() ?: 0)
                result = 31 * result + fixedValues.hashCode()
                result = 31 * result + readOnly.hashCode()
                result
            }
        }
    }
}
