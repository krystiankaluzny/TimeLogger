package org.obywatelgcc.timelogger.model.calendar

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.lang.Long.decode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
data class Calendar(
    val id: Long,
    val accountName: String,
    val accountType: String,
    val displayName: String,
    val ownerName: String
) : Parcelable {
    companion object

    fun description(): String {
        if (accountName == displayName) {
            return accountName
        }

        return "$accountName - $displayName"
    }
}

@Parcelize
data class CalendarEvent(
    val title: String,
    val start: ZonedDateTime,
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