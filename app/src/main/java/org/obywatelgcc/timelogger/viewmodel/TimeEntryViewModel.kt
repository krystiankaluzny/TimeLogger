package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class TimeEntryViewModel : ViewModel(){
    var started by mutableStateOf(false)
    var startDateTime by mutableStateOf(LocalDateTime.now())
    var endDateTime by mutableStateOf(LocalDateTime.now())
    var duration by mutableStateOf(Duration.ZERO)

    fun start() {
        startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        endDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        duration = Duration.between(startDateTime, endDateTime)
        started = true
    }

    fun update() {
        endDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        duration = Duration.between(startDateTime, endDateTime)
    }

    fun stop() {
        started = false
        endDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        duration = Duration.between(startDateTime, endDateTime)
    }

    fun save(description: String) {
        Log.d("TimeEntryViewModel", "save: $description")
    }

    fun updateStartDate(localDate: LocalDate) {
        startDateTime = LocalDateTime.of(localDate, startDateTime.toLocalTime())
        duration = Duration.between(startDateTime, endDateTime)
    }

    fun updateStartTime(localTime: LocalTime) {
        startDateTime = LocalDateTime.of(startDateTime.toLocalDate(), localTime)
        duration = Duration.between(startDateTime, endDateTime)
    }

    fun updateEndDate(localDate: LocalDate) {
        endDateTime = LocalDateTime.of(localDate, endDateTime.toLocalTime())
        duration = Duration.between(startDateTime, endDateTime)
    }

    fun updateEndTime(localTime: LocalTime) {
        endDateTime = LocalDateTime.of(endDateTime.toLocalDate(), localTime)
        duration = Duration.between(startDateTime, endDateTime)
    }
}