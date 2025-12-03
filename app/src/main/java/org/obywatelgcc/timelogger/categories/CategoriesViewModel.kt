package org.obywatelgcc.timelogger.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarRepository

class CategoriesViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

}