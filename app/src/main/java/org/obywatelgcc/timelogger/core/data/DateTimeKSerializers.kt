package org.obywatelgcc.timelogger.core.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {

    private val format = DateTimeFormatter.ISO_ZONED_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.ZonedDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime =
        ZonedDateTime.parse(decoder.decodeString(), format)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(format.format(value))
    }

}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {

    private val format = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString(), format)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(format.format(value))
    }

}

object LocalDateSerializer : KSerializer<LocalDate> {

    private val format = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString(), format)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(format.format(value))
    }

}

object LocalTimeSerializer : KSerializer<LocalTime> {

    private val format = DateTimeFormatter.ISO_LOCAL_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.LocalTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalTime =
        LocalTime.parse(decoder.decodeString(), format)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(format.format(value))
    }

}