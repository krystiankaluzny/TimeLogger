package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.model.DataStoreManager
import org.obywatelgcc.timelogger.model.LocalDateTimeSerializer
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.typeOf

class TimerEventState(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager
) : BaseState(coroutineScope, savedStateHandle, dataStoreManager) {

    private val tickerDelayMs = 1000L
    private var timerPreferencesKey = "timerEventPreferences_v2"
    private val timerEventPreferences =
        jsonDataStoreStateFlow(timerPreferencesKey, TimerEventPreferences())

    val timerState = MutableStateFlow(TimerState.READY_TO_START)

    val eventTitle = stringDataStoreStateFlow("eventTitle", "")

    val startDateTime = saveStateFlow("startDateTime", currentLocalDateTime())
    val endDateTime = saveStateFlow("endDateTime", startDateTime.value)

    val duration = combine(startDateTime, endDateTime) { s, e ->
        Duration.between(s, e)
    }.stateIn(coroutineScope, SharingStarted.Eagerly, Duration.ZERO)

    suspend fun init() {
        Log.d("TimerEventState", "init")
        eventTitle.loadFormDataStore()
        timerEventPreferences.loadFormDataStore()

        updateDataFromPreferences()
    }

    fun updateTitle(title: String) {
        eventTitle.value = title
    }

    fun start() {
        if (timerState.value == TimerState.READY_TO_START) {
            startTimer()
        }
    }

    fun stop() {
        if (timerState.value == TimerState.STARTED) {
            timerState.value = TimerState.STOPPED
            refreshEndDateTime()

            timerEventPreferences.edit { preferences ->
                preferences.copy(
                    timerState = TimerState.STOPPED
                )
            }
        }
    }

    fun resume() {
        if (timerState.value == TimerState.STOPPED) {
            startTimer()
        }
    }


    fun reset() {
        timerState.value = TimerState.READY_TO_START

        val now = currentLocalDateTime()
        startDateTime.value = now
        endDateTime.value = now

        timerEventPreferences.edit { preferences ->
            preferences.copy(
                startDateTime = now
            )
        }
    }

    private fun startTimer() {
        timerState.value = TimerState.STARTED
        coroutineScope.launch {
            while (timerState.value == TimerState.STARTED) {
                refreshEndDateTime()
                delay(tickerDelayMs)
            }
        }

        timerEventPreferences.edit { preferences ->
            preferences.copy(
                timerState = TimerState.STARTED
            )
        }
    }

    private fun refreshEndDateTime() {
        endDateTime.value = currentLocalDateTime()
    }

    fun updateStartDate(localDate: LocalDate) {
        startDateTime.value = LocalDateTime.of(localDate, startDateTime.value.toLocalTime())
        timerEventPreferences.edit { preferences ->
            preferences.copy(
                startDateTime = startDateTime.value
            )
        }
    }

    fun updateStartTime(localTime: LocalTime) {
        startDateTime.value = LocalDateTime.of(startDateTime.value.toLocalDate(), localTime)
        timerEventPreferences.edit { preferences ->
            preferences.copy(
                startDateTime = startDateTime.value
            )
        }
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

    private fun updateDataFromPreferences() {
        if (timerEventPreferences.value.timerState == TimerState.STARTED) {
            start()
        }
    }
}

enum class TimerState {
    READY_TO_START, STARTED, STOPPED
}

@Serializable
data class TimerEventPreferences(
    var title: String = "",
    @Serializable(LocalDateTimeSerializer::class)
    var startDateTime: LocalDateTime = LocalDateTime.MIN,
    var timerState: TimerState = TimerState.READY_TO_START
)