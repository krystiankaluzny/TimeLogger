package org.obywatelgcc.timelogger.timer.presentation.title

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.timer.model.CalendarRepository
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState.Suggestion
import org.obywatelgcc.timelogger.utils.logInfo

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
    }

    private fun loadSuggestions() {
        coroutineScope.launch {
            val search = state.value.eventTitle.trim()
            if(search.length < 3) return@launch

            val events = calendarRepository.findEventsContainsTitle(search)
                .filter { it.title.contains(search, true) }


            val suggestions = events
                .map {
                    val startIndex = it.title.indexOf(search, 0, true)
                    val prefix = it.title.substring(0, startIndex)
                    val match = it.title.substring(startIndex, startIndex + search.length)
                    val suffix = it.title.substring(startIndex + search.length)

                    Suggestion(it.title, it.color, prefix, match, suffix)
                }

            state.update { it.copy(suggestions = suggestions) }
        }
    }
}