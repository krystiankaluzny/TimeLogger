package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class TimeEntryViewModel : ViewModel() {

    private val tickerDelayMs = 1000L

    private val _started = MutableStateFlow(false)
    val started = _started.asStateFlow()

    private val _startDateTime = MutableStateFlow(LocalDateTime.now())
    val startDateTime = _startDateTime.asStateFlow()

    private val _endDateTime = MutableStateFlow(_startDateTime.value)
    val endDateTime = _endDateTime.asStateFlow()

    val duration = combine(startDateTime, endDateTime) { s, e ->
        Duration.between(s, e)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Duration.ZERO)

    val durationStr = duration.map {
        val durationSeconds = it.seconds
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60

        String.format(
            Locale.getDefault(Locale.Category.FORMAT),
            "%d:%02d:%02d", hoursPart, minutesPart, secondsPart
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun start() {
        val now = currentLocalDateTime()
        _startDateTime.value = now
        _endDateTime.value = now
        _started.value = true

        viewModelScope.launch {
            while (_started.value) {
                _endDateTime.value = currentLocalDateTime()
                delay(tickerDelayMs)
            }
        }
    }

    fun stop() {
        _started.value = false
        _endDateTime.value = currentLocalDateTime()
    }

    fun save(description: String) {
        Log.d("TimeEntryViewModel", "save: ${duration.value}")
    }

    fun updateStartDate(localDate: LocalDate) {
        _startDateTime.value = LocalDateTime.of(localDate, _startDateTime.value.toLocalTime())
    }

    fun updateStartTime(localTime: LocalTime) {
        _startDateTime.value = LocalDateTime.of(_startDateTime.value.toLocalDate(), localTime)
    }

    fun updateEndDate(localDate: LocalDate) {
        _endDateTime.value = LocalDateTime.of(localDate, _endDateTime.value.toLocalTime())
    }

    fun updateEndTime(localTime: LocalTime) {
        _endDateTime.value = LocalDateTime.of(_endDateTime.value.toLocalDate(), localTime)
    }

    private fun currentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    }
}