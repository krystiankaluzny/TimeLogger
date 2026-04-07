package org.obywatelgcc.timelogger.settings.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.data.LocalTimeSerializer
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.AvailableCalendarSettings
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

    private val settingsPreferences = jsonDataStoreStateFlow("settingsPreferences", SettingsPreferences())

    suspend fun init(availableCalendars: List<Calendar>, availableEventColors: List<CalendarEventColor>) {
        logInfo("SettingsStateManager init")
        settingsPreferences.loadFormDataStore()
        settingsHolder.updateAvailableCalendarSettings {
            AvailableCalendarSettings(
                allCalendars = availableCalendars,
                allColors = availableEventColors
            )
        }

        val persistedCalendar = settingsPreferences.value.selectedCalendar
        val selectedCalendar = if (availableCalendars.contains(persistedCalendar)) persistedCalendar else Calendar.Empty
        val availableCalendarColors = colorsForCalendar(selectedCalendar)

        settingsHolder.updateCalendarSettings {
            CalendarSettings(
                selectedCalendar = selectedCalendar,
                calendarColors = availableCalendarColors
            )
        }

        settingsHolder.updateSleepWindow {
            SleepWindowSettings(
                enabled = settingsPreferences.value.sleepWindowEnabled,
                start = settingsPreferences.value.sleepWindowStart,
                end = settingsPreferences.value.sleepWindowEnd
            )
        }
        settingsHolder.updateOtherSettings {
            OtherSettings(countUnmeasuredGaps = settingsPreferences.value.countUnmeasuredGaps)
        }
    }

    fun selectCalendar(calendar: Calendar) {
        val colors = colorsForCalendar(calendar)
        settingsPreferences.edit { it.copy(selectedCalendar = calendar, selectedCalendarColors = colors) }
        settingsHolder.updateCalendarSettings {
            it.copy(selectedCalendar = calendar, calendarColors = colors)
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
        settingsHolder.availableCalendarSettings.value.allColors.filter {
            it.accountName == calendar.accountName && it.accountType == calendar.accountType
        }
}

@Serializable
data class SettingsPreferences(
    val selectedCalendar: Calendar = Calendar.Empty,
    val selectedCalendarColors: List<CalendarEventColor> = emptyList(),
    val sleepWindowEnabled: Boolean = false,
    @Serializable(LocalTimeSerializer::class)
    val sleepWindowStart: LocalTime = LocalTime.of(22, 0),
    @Serializable(LocalTimeSerializer::class)
    val sleepWindowEnd: LocalTime = LocalTime.of(10, 0),
    val countUnmeasuredGaps: Boolean = false
)
