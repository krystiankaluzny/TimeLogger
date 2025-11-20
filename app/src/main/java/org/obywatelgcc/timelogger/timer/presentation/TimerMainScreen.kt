package org.obywatelgcc.timelogger.timer.presentation

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.timer.presentation.calendar.CalendarState
import org.obywatelgcc.timelogger.timer.presentation.components.DateTimePickersView
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerState
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import kotlin.collections.forEach


@Composable
fun TimerMainScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    onAction: OnAction
) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TimeLoggerPortraitScreen(
            calendarState,
            timerState,
            titleState,
            settingsState,
            onAction
        )

        else -> TimeLoggerLandscapeScreen(calendarState, timerState, titleState, settingsState, onAction)
    }
}

@Composable
private fun TimeLoggerPortraitScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    onAction: OnAction
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val calendars = calendarState.availableCalendars
        val selectedCalendar = calendarState.selectedCalendar

        CalendarDropdown(
            Modifier.padding(16.dp).fillMaxWidth(),
            calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
        TitleAndColorView(
            titleState,
            calendarState.availableColors,
            calendarState.selectedColor,
            onAction
        )

        HorizontalDivider()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f, fill = false)
        ) {
            DateTimePickersView(
                timerState.startDateTime,
                timerState.endDateTime,
                timerState.runningState != TimerState.RunningState.STARTED,
                onAction
            )
            HorizontalDivider()
            DurationView(timerState.durationStr)
        }

        HorizontalDivider()
        TimeLoggerButtonsView(timerState.runningState, onAction)

        Spacer(Modifier.height(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(70.dp),
            onClick = { onAction(TimerAction.TrySave) }
        ) {
            Text(
                text = settingsState.savingType.description,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimeLoggerLandscapeScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    titleState: TitleState,
    settingsState: SettingsState,
    onAction: OnAction
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val calendars = calendarState.availableCalendars
            val selectedCalendar = calendarState.selectedCalendar

            Box(modifier = Modifier.weight(1.0f)) {
                CalendarDropdown(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
            }
            Box(modifier = Modifier.weight(1.0f)) {
                TitleAndColorView(
                    titleState,
                    calendarState.availableColors,
                    calendarState.selectedColor,
                    onAction
                )
            }
        }
        HorizontalDivider()

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(weight = 1f, fill = false),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DateTimePickersView(
                    timerState.startDateTime,
                    timerState.endDateTime,
                    timerState.runningState != TimerState.RunningState.STARTED,
                    onAction
                )
                HorizontalDivider()
                DurationView(timerState.durationStr)
            }

            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(10.dp))
                TimeLoggerButtonsView(timerState.runningState, onAction)

                Spacer(Modifier.height(10.dp))

                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(70.dp),
                    onClick = { onAction(TimerAction.TrySave) }
                ) {
                    Text(
                        text = settingsState.savingType.description,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
private fun TitleAndColorView(
    titleState: TitleState,
    colors: List<CalendarEventColor>,
    selectedColor: CalendarEventColor,
    onAction: OnAction
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        EventTitleTextField(titleState, onAction)
        ColorDropdown(colors, selectedColor, { onAction(TimerAction.SelectColor(it)) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.EventTitleTextField(
    titleState: TitleState,
    onAction: OnAction
) {
    val eventTitle = titleState.eventTitle
    val titleSuggestions = titleState.suggestions

    var titleFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                eventTitle,
                selection = TextRange(eventTitle.length)
            )
        )
    }
    var expanded by remember { mutableStateOf(false) }

    if(eventTitle != titleFieldValue.text) {
        //restore cursor position
        titleFieldValue = TextFieldValue(eventTitle, selection = TextRange(eventTitle.length))
    }
    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = {
            expanded = it
        },
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .weight(1.0f)
    ) {
        OutlinedTextField(
            value = titleFieldValue,
            onValueChange = {
                titleFieldValue = it
                onAction(TimerAction.UpdateTitle(it.text))
                expanded = true
            },
            label = { Text(text = "Event title") },
            singleLine = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
        )

        if (titleSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { }
            ) {
                titleSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        onClick = {
                            onAction(TimerAction.SelectSuggestion(suggestion))
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {

                                Text(
                                    text = buildAnnotatedString {
                                        append(suggestion.prefix)
                                        withStyle(
                                            SpanStyle(
                                                color = TimeLoggerTheme.extendedColors.suggestionMatch,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(suggestion.match)
                                        }
                                        append(suggestion.suffix)
                                    },
                                    modifier = Modifier
                                        .weight(1.0f)
                                )

                                ColorHolder(
                                    color = suggestion.color,
                                    size = 30.dp,
                                    onClick = {},
                                    showBorder = false
                                )
                            }
                        }
                    )
                }
            }
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.ColorDropdown(
    colors: List<CalendarEventColor>,
    selectedColor: CalendarEventColor?,
    onSelect: (CalendarEventColor) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val mainColorButtonSize = 40.dp

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .width(mainColorButtonSize)
    ) {

        ColorHolder(
            color = selectedColor,
            size = mainColorButtonSize,
            onClick = { expanded = true },
            showBorder = false
        )

        val itemWidth = 55.dp
        val itemHeight = 40.dp
        val columnCount = 6
        val colorButtonSize = itemWidth - 25.dp

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.width(itemWidth * columnCount)
        ) {
            for (rowId in 0..<Math.ceilDiv(colors.size, columnCount)) {
                Row(
                    modifier = Modifier
                ) {
                    for (columnId in 0..<columnCount) {

                        val index = rowId * columnCount + columnId
                        if (index < colors.size) {
                            val c = colors[index]
                            DropdownMenuItem(
                                onClick = {
                                    //handle in Surface.onClick
                                },
                                text = {

                                    ColorHolder(
                                        color = c,
                                        size = colorButtonSize,
                                        onClick = {
                                            onSelect(c)
                                            expanded = false
                                        },
                                        showBorder = c == selectedColor
                                    )
                                },
                                modifier = Modifier
                                    .width(itemWidth)
                                    .height(itemHeight)
                            )
                        }

                    }
                } // end row
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.ColorHolder(
    color: CalendarEventColor?,
    size: Dp,
    onClick: () -> Unit,
    showBorder: Boolean = true
) {
    val border = if (showBorder) BorderStroke(
        TimeLoggerTheme.values.selectedColorBorder,
        TimeLoggerTheme.colorScheme.secondary.copy(alpha = 1f)
    ) else null

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        shape = CircleShape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = color?.colorAsLong()?.let { Color(it) } ?: Color.White
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
        border = border,
        content = {}
    )
}

@Composable
private fun DurationView(durationStr: String) {
    Text(
        text = "Duration: $durationStr",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
    )
}

@Composable
private fun TimeLoggerButtonsView(timerRunningState: TimerState.RunningState, onAction: OnAction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        val buttonModifier = Modifier
            .weight(0.5f)
            .height(50.dp)

        val buttonElevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { onAction(TimerAction.RestartTimer) },
            elevation = buttonElevation,
            enabled = true
        ) {
            Text(text = "Restart")
        }

        Spacer(Modifier.width(16.dp))

        when (timerRunningState) {
            TimerState.RunningState.READY_TO_START -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.StartTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.extendedColors.timerToStartButton,
                    contentColor = TimeLoggerTheme.extendedColors.timerButtonContent
                )
            ) {
                Text(text = "Start")
            }

            TimerState.RunningState.STARTED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.StopTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.extendedColors.timerToStopButton,
                    contentColor = TimeLoggerTheme.extendedColors.timerButtonContent
                )
            ) {
                Text(text = "Stop")
            }

            TimerState.RunningState.STOPPED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.ResumeTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.extendedColors.timerToResumeButton,
                    contentColor = TimeLoggerTheme.extendedColors.timerButtonContent
                )
            ) {
                Text(text = "Resume")
            }
        }
    }
}