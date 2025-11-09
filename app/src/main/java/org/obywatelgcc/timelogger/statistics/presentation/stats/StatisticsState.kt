package org.obywatelgcc.timelogger.statistics.presentation.stats

import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import java.time.Duration

data class StatisticsState(
    val statisticItems: List<StatisticItem> = listOf<StatisticItem>(),
) {
    data class StatisticItem(
        val title: String,
        val totalDuration: Duration,
        val color: CalendarEventColor?
    )
}