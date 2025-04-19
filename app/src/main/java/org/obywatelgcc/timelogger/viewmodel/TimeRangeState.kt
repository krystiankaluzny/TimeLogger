package org.obywatelgcc.timelogger.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class TimeRangeState(
    coroutineScope: CoroutineScope
) {
    val startDateTime = MutableStateFlow(currentLocalDateTime())
    val endDateTime = MutableStateFlow(startDateTime.value)
    val duration = combine(startDateTime, endDateTime) { s, e ->
        Duration.between(s, e)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, Duration.ZERO)

    fun reset() {
        val now = currentLocalDateTime()
        startDateTime.value = now
        endDateTime.value = now
    }

    fun refreshEndDateTime() {
        endDateTime.value = currentLocalDateTime()
    }

    fun updateStartDate(localDate: LocalDate) {
        startDateTime.value = LocalDateTime.of(localDate, startDateTime.value.toLocalTime())
    }

    fun updateStartTime(localTime: LocalTime) {
        startDateTime.value = LocalDateTime.of(startDateTime.value.toLocalDate(), localTime)
    }

    fun updateEndDate(localDate: LocalDate) {
        endDateTime.value = LocalDateTime.of(localDate, endDateTime.value.toLocalTime())
    }

    fun updateEndTime(localTime: LocalTime) {
        endDateTime.value = LocalDateTime.of(endDateTime.value.toLocalDate(), localTime)
    }

    private fun currentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    }
}
