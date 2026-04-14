package org.obywatelgcc.timelogger.core.presentation.components.filter

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.OutlinedTextField
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction.NextRange
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction.PreviousRange
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction.ResetRange
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction.SelectTimeRangeType
import java.time.format.DateTimeFormatter


@Composable
fun FilterTimeRangeView(
    timeRange: ZonedDateTimeRange,
    selectedTimeRangeType: FilterTimeRangeType,
    onAction: (FilterTimeRangeAction) -> Unit
) {
    TimeRangeButtonsView(selectedTimeRangeType, onAction)
    TimeRangeValeView(timeRange, selectedTimeRangeType, onAction)
}

@Composable
fun TimeRangeButtonsView(
    selectedTimeRangeType: FilterTimeRangeType,
    onAction: (FilterTimeRangeAction) -> Unit
) {

    ConstraintLayout {

        val (homeIcon, timeRangeTypes) = createRefs()

        IconButton(
            modifier = Modifier.constrainAs(homeIcon, {
                end.linkTo(timeRangeTypes.start)
                centerVerticallyTo(parent)
            }),
            onClick = { onAction(ResetRange) }) {
            Icon(imageVector = Icons.Filled.Home, contentDescription = "Reset")
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.constrainAs(timeRangeTypes, {
                start.linkTo(homeIcon.end)
                centerVerticallyTo(parent)
                centerHorizontallyTo(parent)
            })
                .height(38.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.DAY)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.DAY,
                label = { Text(text = "1D") }
            )

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.WEEK)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.WEEK,
                label = { Text(text = "1W") }
            )

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.MONTH)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.MONTH,
                label = { Text(text = "1M") }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeValeView(
    timeRange: ZonedDateTimeRange,
    timeRangeType: FilterTimeRangeType,
    onAction: (FilterTimeRangeAction) -> Unit
) {
    val dateRangeStr = when (timeRangeType) {
        FilterTimeRangeType.DAY -> timeRange.from.format(DateTimeFormatter.ISO_LOCAL_DATE)
        FilterTimeRangeType.WEEK ->
            timeRange.from.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                    " \u2014 " + // em dash ( — )
                    timeRange.to.format(DateTimeFormatter.ISO_LOCAL_DATE)

        FilterTimeRangeType.MONTH -> timeRange.from.format(DateTimeFormatter.ofPattern("yyyy MMMM"))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAction(PreviousRange) }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(0.75f),
            value = dateRangeStr,
            onValueChange = {},
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )

        IconButton(onClick = { onAction(NextRange) }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}