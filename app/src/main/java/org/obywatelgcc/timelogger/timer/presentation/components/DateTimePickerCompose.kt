package org.obywatelgcc.timelogger.timer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rexmtorres.android.composedatepicker.datepicker.DatePicker
import io.github.rexmtorres.android.composedatepicker.datepicker.data.DefaultDatePickerConfig
import io.github.rexmtorres.android.composedatepicker.datepicker.data.model.DatePickerDate
import io.github.rexmtorres.android.composedatepicker.datepicker.ui.model.DatePickerConfiguration
import io.github.rexmtorres.android.composedatepicker.timepicker.TimePicker
import io.github.rexmtorres.android.composedatepicker.timepicker.data.DefaultTimePickerConfig
import io.github.rexmtorres.android.composedatepicker.timepicker.data.model.TimePickerTime
import io.github.rexmtorres.android.composedatepicker.timepicker.enums.MinuteGap
import io.github.rexmtorres.android.composedatepicker.timepicker.ui.model.TimePickerConfiguration
import org.obywatelgcc.timelogger.core.presentation.components.CustomButtonDefaults
import org.obywatelgcc.timelogger.core.presentation.components.CustomButtonDefaults.textButtonContentModifier
import org.obywatelgcc.timelogger.core.presentation.components.TextButton
import org.obywatelgcc.timelogger.timer.presentation.TimerAction
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

@Preview
@Composable
fun DateTimePickerRowPreview() {

    DateTimePickersView(LocalDateTime.now().minusHours(3), LocalDateTime.now(), true, {})
}

@Composable
fun DateTimePickersView(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    modifiable: Boolean,
    onAction: (TimerAction) -> Unit
) {

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    var timerDataTimeCheckState by remember { mutableStateOf(TimerDateTimeCheckState()) }

    val offsetButtonValue = 5L


    if (!modifiable) {
        timerDataTimeCheckState = TimerDateTimeCheckState()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 10.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.0f)
            ) {

                CheckableTextButton(timerDataTimeCheckState.startDateChecked, startDateTime.format(dateFormatter)) {
                    if (modifiable) {
                        timerDataTimeCheckState = timerDataTimeCheckState.toggleStartDate()
                    }
                }

                CheckableTextButton(timerDataTimeCheckState.startTimeChecked, startDateTime.format(timeFormatter)) {
                    if (modifiable) {
                        timerDataTimeCheckState = timerDataTimeCheckState.toggleStartTime()
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OffsetButton(-5L, { onAction(TimerAction.OffsetStartTime(-5L, ChronoUnit.MINUTES)) })
                    OffsetButton(+5L, { onAction(TimerAction.OffsetStartTime(+5L, ChronoUnit.MINUTES)) })
                }
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.0f)
            ) {
                CheckableTextButton(timerDataTimeCheckState.endDateChecked, endDateTime.format(dateFormatter)) {
                    if (modifiable) {
                        timerDataTimeCheckState = timerDataTimeCheckState.toggleEndDate()
                    }
                }

                CheckableTextButton(timerDataTimeCheckState.endTimeChecked, endDateTime.format(timeFormatter)) {
                    if (modifiable) {
                        timerDataTimeCheckState = timerDataTimeCheckState.toggleEndTime()
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OffsetButton(-5L, { onAction(TimerAction.OffsetEndTime(-5L, ChronoUnit.MINUTES)) })
                    OffsetButton(+5L, { onAction(TimerAction.OffsetEndTime(+5L, ChronoUnit.MINUTES)) })
                }
            }
        }

        if (timerDataTimeCheckState.anyChecked()) {
            HorizontalDivider()
        }

        val datePickerConfiguration = DatePickerConfiguration.Builder()
            .height(200.dp)
            .numberOfMonthYearRowsDisplayed(5)
            .headerTextStyle(
                DefaultDatePickerConfig.headerTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
            )
            .monthYearTextStyle(
                DefaultDatePickerConfig.monthYearTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onSecondaryContainer)
            )
            .selectedMonthYearTextStyle(
                DefaultDatePickerConfig.selectedMonthYearTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onPrimaryContainer)
            )
            .dateTextStyle(
                DefaultDatePickerConfig.dateTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onSecondaryContainer)
            )
            .selectedDateTextStyle(
                DefaultDatePickerConfig.selectedDateTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onPrimaryContainer)
            )
            .selectedMonthYearAreaColor(TimeLoggerTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .selectedDateBackgroundColor(color = TimeLoggerTheme.colorScheme.primaryContainer)
            .build()

        val timePickerConfig = TimePickerConfiguration.Builder()
            .height(150.dp)
            .numberOfTimeRowsDisplayed(count = 5)
            .selectedTimeScaleFactor(scaleFactor = 1.4f)
            .selectedTimeAreaColor(TimeLoggerTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .timeTextStyle(
                DefaultTimePickerConfig.timeTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onSecondaryContainer)
            )
            .selectedTimeTextStyle(
                DefaultTimePickerConfig.selectedTimeTextStyle
                    .copy(color = TimeLoggerTheme.colorScheme.onPrimaryContainer)
            )
            .build()

        if (timerDataTimeCheckState.startDateChecked) {
            DatePicker(
                id = 1,
                date = startDateTime.toDatePickerDate(),
                configuration = datePickerConfiguration,
                onDateSelected = { date ->
                    val newDate = date.toLocalDate()
                    if (startDateTime.toLocalDate() != newDate) {
                        onAction(TimerAction.UpdateStartDate(newDate))
                    }
                }
            )
        }

        if (timerDataTimeCheckState.startTimeChecked) {
            Box(modifier = Modifier.width(200.dp)) {
                TimePicker(
                    id = 2,
                    is24Hour = true,
                    minuteGap = MinuteGap.FIVE,
                    time = startDateTime.toTimePickerTime(),
                    configuration = timePickerConfig,
                    onTimeSelected = { time ->
                        val newTime = time.toLocalTime()
                        if (startDateTime.toLocalTime() != newTime) {
                            onAction(TimerAction.UpdateStartTime(newTime))
                        }
                    }
                )
            }
        }

        if (timerDataTimeCheckState.endDateChecked) {
            DatePicker(
                id = 3,
                date = endDateTime.toDatePickerDate(),
                configuration = datePickerConfiguration,
                onDateSelected = { date ->
                    val newDate = date.toLocalDate()
                    if (endDateTime.toLocalDate() != newDate) {
                        onAction(TimerAction.UpdateEndDate(newDate))
                    }
                }
            )
        }

        if (timerDataTimeCheckState.endTimeChecked) {
            Box(modifier = Modifier.width(200.dp)) {
                TimePicker(
                    id = 4,
                    is24Hour = true,
                    minuteGap = MinuteGap.FIVE,
                    time = endDateTime.toTimePickerTime(),
                    configuration = timePickerConfig,
                    onTimeSelected = { time ->
                        val newTime = time.toLocalTime()
                        if (endDateTime.toLocalTime() != newTime) {
                            onAction(TimerAction.UpdateEndTime(newTime))
                        }
                    }
                )
            }
        }
    }
}

private data class TimerDateTimeCheckState(
    val startDateChecked: Boolean = false,
    val startTimeChecked: Boolean = false,
    val endDateChecked: Boolean = false,
    val endTimeChecked: Boolean = false
) {
    fun toggleStartDate(): TimerDateTimeCheckState = TimerDateTimeCheckState(startDateChecked = !startDateChecked)
    fun toggleStartTime(): TimerDateTimeCheckState = TimerDateTimeCheckState(startTimeChecked = !startTimeChecked)
    fun toggleEndDate(): TimerDateTimeCheckState = TimerDateTimeCheckState(endDateChecked = !endDateChecked)
    fun toggleEndTime(): TimerDateTimeCheckState = TimerDateTimeCheckState(endTimeChecked = !endTimeChecked)

    fun anyChecked(): Boolean {
        return startDateChecked || startTimeChecked || endDateChecked || endTimeChecked
    }
}

fun TimePickerTime.toLocalTime(): LocalTime = LocalTime.of(hour, minute)
fun LocalDateTime.toTimePickerTime(): TimePickerTime = TimePickerTime(hour, minute)
fun DatePickerDate.toLocalDate(): LocalDate = LocalDate.of(year, month + 1, day)
fun LocalDateTime.toDatePickerDate(): DatePickerDate = DatePickerDate(year, monthValue - 1, dayOfMonth)

@Composable
fun CheckableTextButton(checked: Boolean, text: String, onClick: () -> Unit) {
    val contentColor = TimeLoggerTheme.colorScheme.onSurfaceVariant
    val containerColor = if (checked) TimeLoggerTheme.colorScheme.primaryContainer else Color.Transparent

    TextButton(
        modifier = Modifier.padding(2.dp),
        contentModifier = Modifier.textButtonContentModifier(horizontal = 10.dp, vertical = 4.dp),
        onClick = onClick,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = contentColor,
                containerColor = containerColor
            )
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun OffsetButton(offsetButtonValue: Long, onClick: () -> Unit) {

    val sign = if(offsetButtonValue < 0) "-" else "+"

    val offsetButtonModifier =  Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
    val offsetButtonElevation = CustomButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)

    org.obywatelgcc.timelogger.core.presentation.components.ElevatedButton (
        modifier = offsetButtonModifier,
        onClick = onClick,
        elevation = offsetButtonElevation
    ) {
        Text(text = "${sign}${offsetButtonValue.absoluteValue}m")
    }
}