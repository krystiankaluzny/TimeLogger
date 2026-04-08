package org.obywatelgcc.timelogger.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryState
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryStateManager
import org.obywatelgcc.timelogger.core.cache.Cache
import org.obywatelgcc.timelogger.core.cache.TimedCache
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction
import org.obywatelgcc.timelogger.core.presentation.filter.FilterTimeRangeState
import org.obywatelgcc.timelogger.core.presentation.filter.FilterTimeRangeStateManager
import org.obywatelgcc.timelogger.settings.model.SettingsProvider

class CategoriesViewModel(
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    private val settingsProvider: SettingsProvider,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val filterTimeRangeStateManager = FilterTimeRangeStateManager(
        viewModelScope, savedStateHandle, dataStoreManager, FilterTimeRangeState(),
        "categoriesFilterTimeRangePref"
    )
    private val categoryStateManager = CategoryStateManager(
        viewModelScope, savedStateHandle, dataStoreManager, CategoryState()
    )

    private val calendarEventCache: Cache<Pair<Calendar, ZonedDateTimeRange>, List<CalendarEvent>> =
        TimedCache.expiringEveryMinutes(5L)

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val filterState = filterTimeRangeStateManager.state.asStateFlow()
    val categoryState = categoryStateManager.state.asStateFlow()

    private suspend fun initData() {
        filterTimeRangeStateManager.init()
        categoryStateManager.init()
        viewModelScope.launch {
            settingsProvider.calendarSettings.collect { refreshData() }
        }
        launchRefreshData()
        _initialized.update { true }
    }

    fun onFilterAction(action: FilterTimeRangeAction) = when (action) {
        is FilterTimeRangeAction.SelectTimeRangeType -> {
            filterTimeRangeStateManager.selectTimeRangeType(action.type)
            launchRefreshData()
        }
        FilterTimeRangeAction.PreviousRange -> {
            filterTimeRangeStateManager.calculatePreviousRange()
            launchRefreshData()
        }
        FilterTimeRangeAction.NextRange -> {
            filterTimeRangeStateManager.calculateNextRange()
            launchRefreshData()
        }
        FilterTimeRangeAction.ResetRange -> {
            filterTimeRangeStateManager.resetRange()
            launchRefreshData()
        }
    }

    fun onCategoryAction(action: CategoriesAction) = when (action) {
        is CategoriesAction.SelectCategory -> {
            categoryStateManager.selectCategory(action.index)
            launchRefreshData()
        }
        is CategoriesAction.AddCategory -> categoryStateManager.addCategory(action.name)
        is CategoriesAction.RenameCategory -> categoryStateManager.renameCategory(action.categoryId, action.newName)
        is CategoriesAction.DeleteCategory -> {
            categoryStateManager.deleteCategory(action.categoryId)
            launchRefreshData()
        }
        is CategoriesAction.AddItem -> {
            categoryStateManager.addItem(action.categoryId, action.name)
            launchRefreshData()
        }
        is CategoriesAction.RenameItem -> categoryStateManager.renameItem(action.categoryId, action.itemId, action.newName)
        is CategoriesAction.DeleteItem -> {
            categoryStateManager.deleteItem(action.categoryId, action.itemId)
            launchRefreshData()
        }
        is CategoriesAction.SetItemColor -> categoryStateManager.setItemColor(action.categoryId, action.itemId, action.colorHex)
        is CategoriesAction.AddPattern -> {
            categoryStateManager.addPattern(action.categoryId, action.itemId, action.pattern)
            launchRefreshData()
        }
        is CategoriesAction.RemovePattern -> {
            categoryStateManager.removePattern(action.categoryId, action.itemId, action.pattern)
            launchRefreshData()
        }
        CategoriesAction.ToggleManageMode -> categoryStateManager.toggleManageMode()
    }

    private fun launchRefreshData() {
        viewModelScope.launch { refreshData() }
    }

    private suspend fun refreshData() {
        val selectedCalendar = settingsProvider.calendarSettings.value.selectedCalendar
        val queryTimeRange = filterState.value.timeRange
        val key = selectedCalendar to queryTimeRange
        val events = calendarEventCache.get(key)
            ?: calendarRepository.findEventsInTimeRange(selectedCalendar, queryTimeRange)
                .also { calendarEventCache.put(key, it) }

        categoryStateManager.recalculate(queryTimeRange, events)
    }
}
