package org.obywatelgcc.timelogger.statistics.presentation.filter

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import java.time.ZonedDateTime

data class FilterState(
    val availableCalendars: List<Calendar> = listOf<Calendar>(),
    val selectedCalendar: Calendar = Calendar.Empty,

    val timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY,
    val timeRange: ZonedDateTimeRange = ZonedDateTimeRange(ZonedDateTime.now(), ZonedDateTime.now())
)

enum class FilterTimeRangeType {
    DAY, WEEK, MONTH
}