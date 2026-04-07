package org.obywatelgcc.timelogger.timer.presentation.title

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.CalendarSettings
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState.Suggestion
import org.obywatelgcc.timelogger.utils.logDebug
import org.obywatelgcc.timelogger.utils.logInfo
import kotlin.math.min

class TitleStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: TitleState,
    val calendarRepository: CalendarRepository
) : BaseStateManager<TitleState>(coroutineScope, savedStateHandle, dataStoreManager, initialState) {

    private val eventTitle = stringDataStoreStateFlow("eventTitle", "")
    private val titlePreferences = jsonDataStoreStateFlow("titlePreferences", TitlePreferences())

    suspend fun init(calendarSettings: CalendarSettings) {
        logInfo("init")
        eventTitle.loadFormDataStore()
        titlePreferences.loadFormDataStore()

        if(calendarSettings.selectedCalendar != Calendar.Empty) {
            updateCalendar(calendarSettings)
        }
    }

    fun updateCalendar(calendarSettings: CalendarSettings) {
        val selectedColor =
            if (calendarSettings.calendarColors.contains(titlePreferences.value.selectedColor)) titlePreferences.value.selectedColor
            else calendarSettings.calendarColors.getOrElse(0) { CalendarEventColor.Empty }

        if (titlePreferences.value.colors != calendarSettings.calendarColors) {
            titlePreferences.edit { pref ->
                pref.colors = calendarSettings.calendarColors
                pref.selectedColor = selectedColor
                pref
            }
        }

        state.update {
            it.copy(
                eventTitle = eventTitle.value,
                calendar = calendarSettings.selectedCalendar,
                availableColors = calendarSettings.calendarColors,
                selectedColor = selectedColor
            )
        }
    }

    fun selectColor(color: CalendarEventColor) {
        titlePreferences.edit { pref ->
            pref.selectedColor = color
            pref
        }
        state.update { it.copy(selectedColor = color) }
    }

    fun selectSuggestedColor(color: CalendarEventColor?) {
        if (state.value.availableColors.contains(color)) {
            color?.let { selectColor(it) }
        }
    }

    fun shiftColor() {
        val colors = state.value.availableColors
        val currentColorIndex = colors.indexOf(state.value.selectedColor)
        if (currentColorIndex != -1) {
            if (currentColorIndex == colors.lastIndex) {
                selectColor(colors.first())
            } else {
                selectColor(colors[currentColorIndex + 1])
            }
        }
    }

    fun updateTitle(title: String) {
        state.update { it.copy(eventTitle = title) }
        eventTitle.value = title
        loadSuggestions()
    }

    fun clearTitle() {
        state.update { it.copy(eventTitle = "", suggestions = listOf()) }
        eventTitle.value = ""
    }

    fun selectSuggestion(suggestion: Suggestion) {
        state.update { it.copy(eventTitle = suggestion.value, suggestions = listOf()) }
        eventTitle.value = suggestion.value
    }

    private fun loadSuggestions() {
        coroutineScope.launch {
            val search = state.value.eventTitle.trim()
            val selectedCalendar = state.value.calendar

            if (search.length < 2) {
                state.update { it.copy(suggestions = listOf()) }
                return@launch
            }

            val events = calendarRepository.findEventsContainsTitle(selectedCalendar, search)
                .filter { it.title.contains(search, true) }
                .groupBy { it.title.trim() }
                .values
                .map { it.maxBy { it.timeRange.from } }
                .sortedByDescending { it.timeRange.from }

            logDebug("loadSuggestions, $events")

            val suggestions = events
                .map {
                    val maxSuggestionLength = 60
                    val startIndex = it.title.indexOf(search, 0, true)
                    if (it.title.length < maxSuggestionLength) {
                        val prefix = it.title.substring(0, startIndex)
                        val match = it.title.substring(startIndex, startIndex + search.length)
                        val suffix = it.title.substring(startIndex + search.length)
                        Suggestion(it.title, it.color, prefix, match, suffix)
                    } else {
                        val prefix = it.title.substring(0, min(maxSuggestionLength, startIndex))
                        val match =
                            it.title.substring(prefix.length, min(maxSuggestionLength, prefix.length + search.length))
                        val suffix = if (prefix.length + match.length < maxSuggestionLength)
                            it.title.substring(prefix.length + match.length, maxSuggestionLength) + "..."
                        else "..."
                        Suggestion(it.title, it.color, prefix, match, suffix)
                    }
                }

            state.update { it.copy(suggestions = suggestions) }
        }
    }
}

@Serializable
private data class TitlePreferences(
    var colors: List<CalendarEventColor> = listOf(),
    var selectedColor: CalendarEventColor = CalendarEventColor.Empty
)