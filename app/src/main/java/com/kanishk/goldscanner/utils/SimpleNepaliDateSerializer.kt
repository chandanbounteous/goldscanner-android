package com.kanishk.goldscanner.utils

import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Custom serializer for SimpleDate to enable JSON serialization/deserialization
 */
object SimpleNepaliDateSerializer : KSerializer<SimpleDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "SimpleDate", 
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: SimpleDate) {
        val jsonObject = buildJsonObject {
            put("year", value.year)
            put("month", value.month)
            put("dayOfMonth", value.dayOfMonth)
        }
        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }

    override fun deserialize(decoder: Decoder): SimpleDate {
        val jsonObject = decoder.decodeSerializableValue(JsonObject.serializer())
        return SimpleDate(
            year = jsonObject["year"]!!.jsonPrimitive.int,
            month = jsonObject["month"]!!.jsonPrimitive.int,
            dayOfMonth = jsonObject["dayOfMonth"]!!.jsonPrimitive.int
        )
    }
}