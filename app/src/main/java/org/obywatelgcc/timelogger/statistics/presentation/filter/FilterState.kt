package org.obywatelgcc.timelogger.statistics.presentation.filter

import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import java.time.ZonedDateTime

data class FilterState(
    val timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY,
    val timeRange: ZonedDateTimeRange = ZonedDateTimeRange(ZonedDateTime.now(), ZonedDateTime.now())
)

enum class FilterTimeRangeType {
    DAY, WEEK, MONTH
}
