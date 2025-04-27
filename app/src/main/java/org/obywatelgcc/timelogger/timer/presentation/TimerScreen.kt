package org.obywatelgcc.timelogger.timer.presentation

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEventColor
import org.obywatelgcc.timelogger.timer.presentation.calendar.CalendarState
import org.obywatelgcc.timelogger.timer.presentation.components.DateTimePickerRow
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.LocalDateTime

typealias OnAction = (TimerAction) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootTimerScreen(viewModel: TimerViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val appName by viewModel.appName.collectAsStateWithLifecycle()
    val calenderState by viewModel.calendarState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effectsFlow) {
        when (it) {
            is TimerEffect.ValidationError -> snackbarHostState.showSnackbar(
                it.message,
                "OK",
                duration = SnackbarDuration.Short
            )

            is TimerEffect.SavingMessage -> snackbarHostState.showSnackbar(
                it.message,
                "OK",
                duration = SnackbarDuration.Short
            )
        }

    }

    Scaffold(
        topBar = { TopBar(appName) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val initialized by viewModel.initialized.collectAsStateWithLifecycle()

        if (initialized) {
            TimeLoggerScreen(calenderState, timerState, innerPadding, viewModel::onAction)
        } else {
            LoadingScreen(innerPadding)
        }
    }

}

@Composable
private fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onEvent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(appName: String, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val expandedHeight = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TopAppBarDefaults.TopAppBarExpandedHeight
        else -> TopAppBarDefaults.TopAppBarExpandedHeight / 2
    }

    TopAppBar(
        title = { Text(text = appName) },
        expandedHeight = expandedHeight
    )
}

@Composable
private fun LoadingScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TimeLoggerScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TimeLoggerPortraitScreen(
            calendarState,
            timerState,
            innerPadding,
            onAction
        )

        else -> TimeLoggerLandscapeScreen(calendarState, timerState, innerPadding, onAction)
    }
}

@Composable
fun TimeLoggerPortraitScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding),
    ) {
        val calendars = calendarState.availableCalendars
        val selectedCalendar = calendarState.selectedCalendar

        CalendarDropdown(calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
        TitleAndColorRow(timerState.eventTitle, calendarState.availableColors, calendarState.selectedColor, onAction)

        StartDateTimeRow(timerState.startDateTime, onAction)
        Spacer(Modifier.height(10.dp))
        EndDateTimeRow(timerState.endDateTime, onAction)
        DurationRow(timerState.durationStr)
        TimeLoggerButtonsRow(timerState.runningState, onAction)

        Spacer(Modifier.height(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(70.dp),
            onClick = { onAction(TimerAction.TrySave) }) {
            Text(text = "Save")
        }
    }
}

@Composable
fun TimeLoggerLandscapeScreen(
    calendarState: CalendarState,
    timerState: TimerState,
    innerPadding: PaddingValues,
    onAction: OnAction
) {

    Column(
        modifier = Modifier.padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val calendars = calendarState.availableCalendars
            val selectedCalendar = calendarState.selectedCalendar

            Box(modifier = Modifier.weight(1.0f)) {
                CalendarDropdown(calendars, selectedCalendar, { onAction(TimerAction.SelectCalendar(it)) })
            }
            Box(modifier = Modifier.weight(1.0f)) {
                TitleAndColorRow(
                    timerState.eventTitle,
                    calendarState.availableColors,
                    calendarState.selectedColor,
                    onAction
                )
            }
        }

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StartDateTimeRow(timerState.startDateTime, onAction)
                Spacer(Modifier.height(10.dp))
                EndDateTimeRow(timerState.endDateTime, onAction)
            }

            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DurationRow(timerState.durationStr)
                TimeLoggerButtonsRow(timerState.runningState, onAction)

                Spacer(Modifier.height(10.dp))

                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(70.dp),
                    onClick = { onAction(TimerAction.TrySave) }) {
                    Text(text = "Save")
                }
            }
        }


    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDropdown(
    calendars: List<Calendar>,
    selected: Calendar?,
    onSelect: (Calendar) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldState by remember { mutableStateOf(selected?.description() ?: "") }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextField(
            value = textFieldState,
            onValueChange = { },
            singleLine = true,
            label = { Text("Calendar") },
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            calendars.forEach { c ->
                DropdownMenuItem(onClick = {
                    textFieldState = c.description()
                    onSelect(c)
                    expanded = false
                }, text = {
                    Text(text = c.description())
                })
            }
        }
    }
}

@Composable
private fun TitleAndColorRow(
    eventTitle: String,
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
        EntryTitleTextField(eventTitle, { onAction(TimerAction.UpdateTitle(it)) })
        ColorDropdown(colors, selectedColor, { onAction(TimerAction.SelectColor(it)) })
    }
}

@Composable
private fun RowScope.EntryTitleTextField(
    initValue: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = initValue,
        onValueChange = onValueChange,
        label = { Text(text = "Event title") },
        singleLine = true,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .weight(1.0f)
    )
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
        1.dp,
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
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
private fun StartDateTimeRow(startDateTime: LocalDateTime, onAction: OnAction) {
    DateTimePickerRow(
        "Start",
        startDateTime,
        { localDate -> onAction(TimerAction.UpdateStartDate(localDate)) },
        { localTime -> onAction(TimerAction.UpdateStartTime(localTime)) })
}

@Composable
private fun EndDateTimeRow(endDateTime: LocalDateTime, onAction: OnAction) {
    DateTimePickerRow(
        "End",
        endDateTime,
        { localDate -> onAction(TimerAction.UpdateEndDate(localDate)) },
        { localTime -> onAction(TimerAction.UpdateEndTime(localTime)) })
}

@Composable
private fun DurationRow(durationStr: String) {
    Text(
        text = "Duration: $durationStr",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
    )
}

@Composable
private fun TimeLoggerButtonsRow(timerRunningState: TimerState.RunningState, onAction: OnAction) {
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
            enabled = (timerRunningState == TimerState.RunningState.READY_TO_START || timerRunningState == TimerState.RunningState.STOPPED)
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
                    containerColor = TimeLoggerTheme.colors.timerToStartButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Start")
            }

            TimerState.RunningState.STARTED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.StopTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToStopButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Stop")
            }

            TimerState.RunningState.STOPPED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { onAction(TimerAction.ResumeTimer) },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToResumeButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Resume")
            }
        }
    }
}