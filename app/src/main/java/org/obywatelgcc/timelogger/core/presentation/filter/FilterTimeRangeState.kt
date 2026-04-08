package org.obywatelgcc.timelogger.core.presentation.filter

import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeType
import java.time.ZonedDateTime

data class FilterTimeRangeState(
    val timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY,
    val timeRange: ZonedDateTimeRange = ZonedDateTimeRange(ZonedDateTime.now(), ZonedDateTime.now())
)

