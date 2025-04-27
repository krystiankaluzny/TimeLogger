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

    private val timerEventPreferences =
        jsonDataStoreStateFlow("timerEventPreferences_v3", TimerEventPreferences())
    private val eventTitle = stringDataStoreStateFlow("eventTitle", "")

    suspend fun init() {
        logInfo("init")
        val now = currentLocalDateTime()
        state.update { it.copy(startDateTime = now, endDateTime = now) }

        eventTitle.loadFormDataStore()
        timerEventPreferences.loadFormDataStore()

        state.update {
            it.copy(
                eventTitle = eventTitle.value,
                startDateTime = timerEventPreferences.value.startDateTime,
                runningState = timerEventPreferences.value.timerRunningState
            )
        }

        if(timerEventPreferences.value.timerRunningState == RunningState.STARTED) {
            startTimer()
        }
    }

    fun updateTitle(title: String) {
        state.update { it.copy(eventTitle = title) }
    }

    fun start() {
        if (state.value.runningState == RunningState.READY_TO_START) {
            startTimer()
        }
    }

    fun stop() {
        if (state.value.runningState == RunningState.STARTED) {
            state.update {
                it.copy(
                    runningState = RunningState.STOPPED,
                    endDateTime = currentLocalDateTime()
                )
            }

            timerEventPreferences.edit { preferences -> preferences.copy(timerRunningState = RunningState.STOPPED) }
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

        timerEventPreferences.edit { preferences -> preferences.copy(startDateTime = now) }
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
    }

    fun updateEndTime(localTime: LocalTime) {
        state.update { it.copy(endDateTime = LocalDateTime.of(it.endDateTime.toLocalDate(), localTime)) }
    }

    private fun refreshEndDateTime() {
        state.update { it.copy(endDateTime = currentLocalDateTime()) }
    }

    private fun currentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    }
}


@Serializable
data class TimerEventPreferences(
    var title: String = "",
    @Serializable(LocalDateTimeSerializer::class)
    var startDateTime: LocalDateTime = LocalDateTime.MIN,
    var timerRunningState: RunningState = RunningState.READY_TO_START
)