package org.obywatelgcc.timelogger.timer.presentation.timer

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.data.LocalDateTimeSerializer
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerState.RunningState
import org.obywatelgcc.timelogger.utils.logInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class TimerStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: TimerState
) : BaseStateManager<TimerState>(coroutineScope, savedStateHandle, dataStoreManager, initialState) {

    private val tickerDelayMs = 1000L

    private val timerEventPreferences = jsonDataStoreStateFlow("timerEventPreferences_v4", TimerEventPreferences())

    suspend fun init() {
        logInfo("init")
        val now = currentLocalDateTime()
        state.update { it.copy(startDateTime = now, endDateTime = now) }

        timerEventPreferences.loadFormDataStore()
        if (timerEventPreferences.value.endDateTime < timerEventPreferences.value.startDateTime) {
            timerEventPreferences.edit { preferences ->
                preferences.copy(
                    endDateTime = timerEventPreferences.value.startDateTime
                )
            }
        }

        state.update {
            it.copy(
                startDateTime = timerEventPreferences.value.startDateTime,
                endDateTime = timerEventPreferences.value.endDateTime,
                runningState = timerEventPreferences.value.timerRunningState
            )
        }

        if (timerEventPreferences.value.timerRunningState == RunningState.STARTED) {
            startTimer()
        }
    }

    fun start() {
        if (state.value.runningState == RunningState.READY_TO_START) {
            startTimer()
        }
    }

    fun stop() {
        if (state.value.runningState == RunningState.STARTED) {
            val now = currentLocalDateTime()
            state.update {
                it.copy(
                    runningState = RunningState.STOPPED,
                    endDateTime = now
                )
            }

            timerEventPreferences.edit { preferences ->
                preferences.copy(
                    timerRunningState = RunningState.STOPPED,
                    endDateTime = now
                )
            }
        }
    }

    fun resume() {
        if (state.value.runningState == RunningState.STOPPED) {
            startTimer()
        }
    }

    fun reset() {
        val now = currentLocalDateTime()
        state.update {
            it.copy(
                runningState = RunningState.READY_TO_START,
                startDateTime = now,
                endDateTime = now
            )
        }

        timerEventPreferences.edit { preferences ->
            preferences.copy(
                timerRunningState = state.value.runningState,
                startDateTime = state.value.startDateTime,
                endDateTime = state.value.endDateTime
            )
        }
    }

    private fun startTimer() {
        state.update { it.copy(runningState = RunningState.STARTED) }
        coroutineScope.launch {
            while (state.value.runningState == RunningState.STARTED) {
                refreshEndDateTime()
                delay(tickerDelayMs)
            }
        }

        timerEventPreferences.edit { preferences -> preferences.copy(timerRunningState = RunningState.STARTED) }
    }

    fun updateStartDate(localDate: LocalDate) {
        state.update { it.copy(startDateTime = LocalDateTime.of(localDate, it.startDateTime.toLocalTime())) }
        timerEventPreferences.edit { preferences -> preferences.copy(startDateTime = state.value.startDateTime) }
    }

    fun updateStartTime(localTime: LocalTime) {
        state.update { it.copy(startDateTime = LocalDateTime.of(it.startDateTime.toLocalDate(), localTime)) }
        timerEventPreferences.edit { preferences -> preferences.copy(startDateTime = state.value.startDateTime) }
    }

    fun updateEndDate(localDate: LocalDate) {
        state.update { it.copy(endDateTime = LocalDateTime.of(localDate, it.endDateTime.toLocalTime())) }
        timerEventPreferences.edit { preferences -> preferences.copy(endDateTime = state.value.endDateTime) }
    }

    fun updateEndTime(localTime: LocalTime) {
        state.update { it.copy(endDateTime = LocalDateTime.of(it.endDateTime.toLocalDate(), localTime)) }
        timerEventPreferences.edit { preferences -> preferences.copy(endDateTime = state.value.endDateTime) }
    }

    private fun refreshEndDateTime() {
        state.update { it.copy(endDateTime = currentLocalDateTime()) }
        timerEventPreferences.edit { preferences -> preferences.copy(endDateTime = state.value.endDateTime) }
    }

    private fun currentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    }

    fun offsetStartTime(value: Long, unit: ChronoUnit) {
        state.update { it.copy(startDateTime = it.startDateTime.plus(value, unit)) }
        timerEventPreferences.edit { preferences -> preferences.copy(startDateTime = state.value.startDateTime) }
    }
}


@Serializable
data class TimerEventPreferences(
    @Serializable(LocalDateTimeSerializer::class)
    var startDateTime: LocalDateTime = MIN,
    @Serializable(LocalDateTimeSerializer::class)
    var endDateTime: LocalDateTime = MIN,
    var timerRunningState: RunningState = RunningState.READY_TO_START
) {
    companion object {
        private val MIN = LocalDateTime.of(2000, 1, 1, 12, 0)
    }
}