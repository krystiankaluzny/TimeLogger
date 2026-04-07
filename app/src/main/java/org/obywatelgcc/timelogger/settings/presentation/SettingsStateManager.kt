package org.obywatelgcc.timelogger.settings.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.data.LocalTimeSerializer
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.CalendarSettings
import org.obywatelgcc.timelogger.settings.model.OtherSettings
import org.obywatelgcc.timelogger.settings.model.SettingsHolder
import org.obywatelgcc.timelogger.settings.model.SleepWindowSettings
import org.obywatelgcc.timelogger.utils.logInfo
import java.time.LocalTime

class SettingsStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    private val settingsHolder: SettingsHolder
) : BaseStateManager<Any>(coroutineScope, savedStateHandle, dataStoreManager, "") {

    companion object {
        const val PREFS_KEY = "settings_v1"
    }

    private val settingsPreferences = jsonDataStoreStateFlow(PREFS_KEY, SettingsPreferences())

    private var allEventColors: List<CalendarEventColor> = emptyList()

    suspend fun init() {
        logInfo("SettingsStateManager init")
        settingsPreferences.loadFormDataStore()
        settingsHolder.updateSleepWindow {
            SleepWindowSettings(
                enabled = settingsPreferences.value.sleepWindowEnabled,
                start = settingsPreferences.value.sleepWindowStart,
                end = settingsPreferences.value.sleepWindowEnd
            )
        }
        settingsHolder.updateOtherSettings {
            OtherSettings(
                countUnmeasuredGaps = settingsPreferences.value.countUnmeasuredGaps
            )
        }
    }

    suspend fun initCalendar(calendars: List<Calendar>, eventColors: List<CalendarEventColor>) {
        logInfo("SettingsStateManager initCalendar")
        allEventColors = eventColors

        val persisted = settingsPreferences.value.selectedCalendar
        val selected = if (calendars.contains(persisted)) persisted
                       else calendars.getOrElse(0) { Calendar.Empty }

        if (selected != persisted) {
            settingsPreferences.edit { it.copy(selectedCalendar = selected) }
        }

        val colors = colorsForCalendar(selected)
        settingsHolder.updateCalendarSettings {
            CalendarSettings(
                selectedCalendar = selected,
                availableCalendars = calendars,
                availableColors = colors
            )
        }
    }

    fun selectCalendar(calendar: Calendar) {
        settingsPreferences.edit { it.copy(selectedCalendar = calendar) }
        val colors = colorsForCalendar(calendar)
        settingsHolder.updateCalendarSettings {
            it.copy(
                selectedCalendar = calendar,
                availableColors = colors
            )
        }
    }

    fun setSleepWindowEnabled(enabled: Boolean) {
        settingsPreferences.edit { it.copy(sleepWindowEnabled = enabled) }
        settingsHolder.updateSleepWindow { it.copy(enabled = enabled) }
    }

    fun setSleepWindowStart(time: LocalTime) {
        settingsPreferences.edit { it.copy(sleepWindowStart = time) }
        settingsHolder.updateSleepWindow { it.copy(start = time) }
    }

    fun setSleepWindowEnd(time: LocalTime) {
        settingsPreferences.edit { it.copy(sleepWindowEnd = time) }
        settingsHolder.updateSleepWindow { it.copy(end = time) }
    }

    fun setCountUnmeasuredGaps(enabled: Boolean) {
        settingsPreferences.edit { it.copy(countUnmeasuredGaps = enabled) }
        settingsHolder.updateOtherSettings { it.copy(countUnmeasuredGaps = enabled) }
    }

    private fun colorsForCalendar(calendar: Calendar): List<CalendarEventColor> =
        allEventColors.filter {
            it.accountName == calendar.accountName && it.accountType == calendar.accountType
        }
}

@Serializable
data class SettingsPreferences(
    val sleepWindowEnabled: Boolean = false,
    @Serializable(LocalTimeSerializer::class)
    val sleepWindowStart: LocalTime = LocalTime.of(22, 0),
    @Serializable(LocalTimeSerializer::class)
    val sleepWindowEnd: LocalTime = LocalTime.of(10, 0),
    val countUnmeasuredGaps: Boolean = false,
    val selectedCalendar: Calendar = Calendar.Empty
)
