package org.obywatelgcc.timelogger.statistics

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterTimeRangeType

sealed interface StatisticsAction {
    data class SelectCalendar(val calendar: Calendar) : StatisticsAction
    data class SelectTimeRangeType(val type: FilterTimeRangeType) : StatisticsAction

    data object PreviousRange: StatisticsAction
    data object NextRange: StatisticsAction
    data object ResetRange: StatisticsAction
}