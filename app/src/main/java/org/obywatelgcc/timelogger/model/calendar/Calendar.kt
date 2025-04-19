package org.obywatelgcc.timelogger.model.calendar

import java.time.LocalDateTime

data class Calendar(
    val id : Long,
    val accountName: String,
    val displayName: String,
    val ownerName: String
) {
    fun description(): String {
        if(accountName == displayName) {
            return accountName
        }

        return "$accountName - $displayName"
    }
}

data class CalendarEntry(
    val description: String,
    val start: LocalDateTime,
    val end: LocalDateTime
)