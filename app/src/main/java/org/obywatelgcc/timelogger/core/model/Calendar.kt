package org.obywatelgcc.timelogger.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.ZonedDateTimeSerializer
import java.lang.Long.decode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
@Serializable
data class Calendar(
    val id: Long,
    val accountName: String,
    val accountType: String,
    val displayName: String,
    val ownerName: String
) : Parcelable {

    companion object {
        val Empty = Calendar(
            id = Long.MIN_VALUE,
            accountName = "",
            accountType = "",
            displayName = "",
            ownerName = ""
        )
    }

    fun description(): String {
        if (accountName == displayName) {
            return accountName
        }

        return "$accountName - $displayName"
    }
}

@Parcelize
@Serializable
data class CalendarEvent(
    val title: String,
    val timeRange: ZonedDateTimeRange,
    val color: CalendarEventColor?
) : Parcelable {
    companion object {
        fun of(
            title: String, start: LocalDateTime, end: LocalDateTime, color: CalendarEventColor?
        ): CalendarEvent {
            return CalendarEvent(title, ZonedDateTimeRange.of(start, end), color)
        }

        fun of(
            title: String, startMillis: Long, endMillis: Long, zoneIdName: String, color: CalendarEventColor?
        ): CalendarEvent {
            return CalendarEvent(title, ZonedDateTimeRange.of(startMillis, endMillis, zoneIdName), color)
        }
    }

}

@Parcelize
@Serializable
data class ZonedDateTimeRange(
    @Serializable(ZonedDateTimeSerializer::class)
    val from: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class)
    val to: ZonedDateTime
) : Parcelable {
    companion object {
        fun of(from: LocalDateTime, to: LocalDateTime): ZonedDateTimeRange {
            val zoneId = ZoneId.systemDefault()
            val fromZoned = from.atZone(zoneId)
            val toZoned = to.atZone(zoneId)
            return ZonedDateTimeRange(fromZoned, toZoned)
        }

        fun of(startMillis: Long, endMillis: Long, zoneIdName: String): ZonedDateTimeRange {
            val zoneId = ZoneId.of(zoneIdName)
            val fromZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zoneId)
            val toZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zoneId)
            return ZonedDateTimeRange(fromZoned, toZoned)
        }
    }

    fun fromMillis() = from.toEpochSecond() * 1000
    fun toMillis() = to.toEpochSecond() * 1000
    fun zoneIdName(): String = to.zone.id
}

@Parcelize
@Serializable
data class CalendarEventColor(
    val key: String,
    val color: String,
    val type: String,
    val accountName: String,
    val accountType: String,
) : Parcelable {

    companion object {
        val Empty = CalendarEventColor(
            key = "empty",
            color = "#00000000",
            type = "empty",
            accountName = "",
            accountType = ""
        )
    }


    fun colorAsLong(): Long {
        return decode(color)
    }
}


