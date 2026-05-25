package com.yonatankarp.ff4k.serialization

import kotlinx.datetime.TimeZone
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object TimeZoneSerializer : KSerializer<TimeZone> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.yonatankarp.ff4k.TimeZone", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: TimeZone,
    ) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): TimeZone = TimeZone.of(decoder.decodeString())
}
