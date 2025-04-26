package org.obywatelgcc.timelogger.timer.model.calendar

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.ZonedDateTimeSerializer
import java.lang.Long.decode
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
    @Serializable(ZonedDateTimeSerializer::class)
    val start: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class)
    val end: ZonedDateTime,
    val color: CalendarEventColor?
) : Parcelable {
    companion object {
        fun of(
            title: String, start: LocalDateTime, end: LocalDateTime, color: CalendarEventColor?
        ): CalendarEvent {
            val zoneId = ZoneId.systemDefault()
            val starZoned = start.atZone(zoneId)
            val endZoned = end.atZone(zoneId)
            return CalendarEvent(title, starZoned, endZoned, color)
        }
    }

    fun startMillis() = start.toEpochSecond() * 1000
    fun endMillis() = end.toEpochSecond() * 1000
    fun zoneIdName(): String = end.zone.id
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
    fun colorAsLong(): Long {
        return decode(color)
    }
}


