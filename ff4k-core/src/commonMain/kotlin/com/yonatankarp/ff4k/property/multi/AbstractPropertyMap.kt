package com.yonatankarp.ff4k.property.multi

import com.yonatankarp.ff4k.property.Property

private typealias Entries<T> = MutableSet<MutableMap.MutableEntry<String, T>>
private typealias Values<V> = MutableCollection<V>

/**
 * SuperClass for property as maps.
 *
 * @author Yonatan Karp-Rudin (@yonatankarp)
 *
 * @param <T> current inner type
 * @param <M> current map type
 */
abstract class AbstractPropertyMap<T, M : MutableMap<String, out T>>(
    override val name: String,
    override val value: M,
    override val description: String? = null,
    override val fixedValues: Set<M> = emptySet(),
    override val readOnly: Boolean = false,
) : Property<M>,
    MutableMap<String, T> {

    init {
        require(fixedValues.isEmpty() || fixedValues.any { it == value }) {
            "Invalid value '$value' for property '$name'. Must be one of: $fixedValues"
        }
    }

    override val size: Int
        get() = value.size

    override fun isEmpty(): Boolean = value.isEmpty()

    override fun containsKey(key: String): Boolean = key in value

    override fun containsValue(value: T): Boolean = value in this.value.values

    override operator fun get(key: String): T? = value[key]

    override fun remove(key: String): T? = value.remove(key)

    operator fun minusAssign(key: String) {
        remove(key)
    }

    override fun clear() = value.clear()

    override val keys: MutableSet<String>
        get() = value.keys

    @Suppress("UNCHECKED_CAST")
    override val entries: Entries<T>
        get() = value.entries as Entries<T>

    @Suppress("UNCHECKED_CAST")
    override val values: Values<T>
        get() = value.values as Values<T>

    @Suppress("UNCHECKED_CAST")
    override fun put(key: String, value: T): T? = (this.value as MutableMap<String, T>).put(key, value)

    @Suppress("UNCHECKED_CAST")
    override fun putAll(from: Map<out String, T>) {
        (value as MutableMap<String, T>).putAll(from)
    }
}
