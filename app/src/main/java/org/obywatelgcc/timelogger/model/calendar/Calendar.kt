package org.obywatelgcc.timelogger.model.calendar

import java.time.LocalDateTime

data class Calendar(
    val id : Long,
    val displayName: String,
    val ownerName: String,
    val accountName: String
) {
    fun description(): String {
        if(ownerName == displayName) {
            return displayName
        }

        return "$ownerName - $displayName"
    }
}

data class CalendarEntry(
    val description: String,
    val start: LocalDateTime,
    val end: LocalDateTime
)