package org.obywatelgcc.timelogger.statistics

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeView
import org.obywatelgcc.timelogger.core.presentation.components.chart.BarChart
import org.obywatelgcc.timelogger.core.presentation.components.chart.BarChartProperties
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.DurationScale
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.Duration

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val statisticsState by viewModel.statisticsState.collectAsStateWithLifecycle()

    val timeRangeType = filterState.timeRangeType
    val timeRange = filterState.timeRange

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (initialized) {
        if (isLandscape) {
            Row {
                Column(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilterTimeRangeView(timeRange, timeRangeType, viewModel::onFilterAction)
                    StatisticsInfoView(statisticsState)
                }
                Column(
                    modifier = Modifier
                        .weight(4f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatisticsBarChartView(statisticsState)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilterTimeRangeView(timeRange, timeRangeType, viewModel::onFilterAction)
                StatisticsInfoView(statisticsState)
                StatisticsBarChartView(statisticsState)
            }
        }
    }
}

@Composable
fun StatisticsInfoView(statisticsState: StatisticsState) {
    val statisticItems = statisticsState.statisticItems
    val totalDuration = statisticItems.fold(Duration.ZERO) { acc, item -> acc.plus(item.totalDuration) }

    Text(text = "Total: ${totalDuration.toHours()}h ${totalDuration.toMinutesPart()}m")
}

@Composable
fun StatisticsBarChartView(statisticsState: StatisticsState) {
    val statisticItems = statisticsState.statisticItems
    val data = statisticItems
        .map {
            Data(
                label = it.title,
                value = it.totalDuration,
                color = it.color?.colorAsLong()?.let { Color(it) } ?: Color.White
            )
        }

    if (data.isNotEmpty()) {
        BarChart(
            modifier = Modifier.padding(horizontal = 22.dp),
            data = data,
            scale = DurationScale(domain = DurationScale.minMaxDomain(data)),
            properties = BarChartProperties.of(
                textColor = TimeLoggerTheme.colorScheme.onSurface,
                axisLineColor = TimeLoggerTheme.colorScheme.onSurface
            ).copy(
                barValueTextInsideBar = false
            )
        )
    } else {
        Text(text = "No data")
    }
}
