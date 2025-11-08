package org.obywatelgcc.timelogger.timer.presentation.title

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarRepository
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

    suspend fun init() {
        logInfo("init")

        eventTitle.loadFormDataStore()

        state.update {
            it.copy(
                eventTitle = eventTitle.value,
            )
        }
    }

    fun updateTitle(title: String, selectedCalendar: Calendar) {
        state.update { it.copy(eventTitle = title) }
        eventTitle.value = title
        loadSuggestions(selectedCalendar)
    }

    fun clearTitle() {
        state.update { it.copy(eventTitle = "", suggestions = listOf()) }
        eventTitle.value = ""
    }

    fun selectSuggestion(suggestion: Suggestion) {
        state.update { it.copy(eventTitle = suggestion.value, suggestions = listOf()) }
        eventTitle.value = suggestion.value
    }

    private fun loadSuggestions(selectedCalendar: Calendar) {
        coroutineScope.launch {
            val search = state.value.eventTitle.trim()
            if (search.length < 2) {
                state.update { it.copy(suggestions = listOf()) }
                return@launch
            }

            val events = calendarRepository.findEventsContainsTitle(selectedCalendar, search)
                .filter { it.title.contains(search, true) }
                .groupBy { it.title.trim() }
                .values
                .map { it.maxBy { it.start } }
                .sortedByDescending { it.start }

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
                        val match = it.title.substring(prefix.length, min(maxSuggestionLength, prefix.length + search.length))
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