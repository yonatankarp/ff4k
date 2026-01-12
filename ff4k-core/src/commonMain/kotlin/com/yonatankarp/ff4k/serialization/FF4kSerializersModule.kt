package com.yonatankarp.ff4k.serialization

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import com.yonatankarp.ff4k.core.FlippingStrategy
import com.yonatankarp.ff4k.property.Property
import com.yonatankarp.ff4k.property.PropertyBigDecimal
import com.yonatankarp.ff4k.property.PropertyBigInteger
import com.yonatankarp.ff4k.property.PropertyBoolean
import com.yonatankarp.ff4k.property.PropertyByte
import com.yonatankarp.ff4k.property.PropertyDouble
import com.yonatankarp.ff4k.property.PropertyFloat
import com.yonatankarp.ff4k.property.PropertyInstant
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyLocalDate
import com.yonatankarp.ff4k.property.PropertyLocalDateTime
import com.yonatankarp.ff4k.property.PropertyLogLevel
import com.yonatankarp.ff4k.property.PropertyLong
import com.yonatankarp.ff4k.property.PropertyShort
import com.yonatankarp.ff4k.property.PropertyString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic

val ff4kSerializersModule = SerializersModule {
    polymorphic(Property::class) {
        subclass(PropertyInt::class, PropertyInt.serializer())
        subclass(PropertyString::class, PropertyString.serializer())
        subclass(PropertyDouble::class, PropertyDouble.serializer())
        subclass(PropertyFloat::class, PropertyFloat.serializer())
        subclass(PropertyLong::class, PropertyLong.serializer())
        subclass(PropertyBoolean::class, PropertyBoolean.serializer())
        subclass(PropertyLocalDateTime::class, PropertyLocalDateTime.serializer())
        subclass(PropertyLogLevel::class, PropertyLogLevel.serializer())
        subclass(PropertyBigInteger::class, PropertyBigInteger.serializer())
        subclass(PropertyBigDecimal::class, PropertyBigDecimal.serializer())
        subclass(PropertyLocalDate::class, PropertyLocalDate.serializer())
        subclass(PropertyInstant::class, PropertyInstant.serializer())
        subclass(PropertyShort::class, PropertyShort.serializer())
        subclass(PropertyByte::class, PropertyByte.serializer())
    }

    polymorphic(FlippingStrategy::class) {
        // Register FlippingStrategy implementations here as they are created
    }
} + humanReadableSerializerModule
