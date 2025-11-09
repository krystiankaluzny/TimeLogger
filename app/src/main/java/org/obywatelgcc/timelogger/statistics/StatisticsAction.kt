package org.obywatelgcc.timelogger.statistics

import org.obywatelgcc.timelogger.core.model.Calendar

sealed interface StatisticsAction {
    data class SelectCalendar(val calendar: Calendar) : StatisticsAction
}