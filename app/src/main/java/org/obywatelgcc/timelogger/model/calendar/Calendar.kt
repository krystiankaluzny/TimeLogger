package org.obywatelgcc.timelogger.model.calendar

import java.lang.Long.decode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class Calendar(
    val id: Long,
    val accountName: String,
    val displayName: String,
    val ownerName: String
) {
    companion object

    fun description(): String {
        if (accountName == displayName) {
            return accountName
        }

        return "$accountName - $displayName"
    }
}

data class CalendarEvent(
    val title: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
    companion object {
        fun of(
            title: String, start: LocalDateTime, end: LocalDateTime
        ): CalendarEvent {
            val zoneId = ZoneId.systemDefault()
            val starZoned = start.atZone(zoneId)
            val endZoned = end.atZone(zoneId)
            return CalendarEvent(title, starZoned, endZoned)
        }
    }

    fun startMillis() = start.toEpochSecond() * 1000
    fun endMillis() = end.toEpochSecond() * 1000
    fun zoneIdName(): String = end.zone.id
}

data class CalendarEventColor(
    val key: String,
    val color: String,
    val type: String
) {
    fun colorAsLong(): Long {
        return decode(color)
    }
}