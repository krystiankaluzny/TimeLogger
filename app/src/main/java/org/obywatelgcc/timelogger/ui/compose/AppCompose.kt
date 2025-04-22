package org.obywatelgcc.timelogger.ui.compose

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
import androidx.compose.runtime.collectAsState
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
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEventColor
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import org.obywatelgcc.timelogger.viewmodel.State
import org.obywatelgcc.timelogger.viewmodel.TimeEventViewModel
import org.obywatelgcc.timelogger.viewmodel.TimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: TimeEventViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val appName by viewModel.appName.collectAsState()

    ObserveAsEvents(viewModel.messageChannelFlow) {
        snackbarHostState.showSnackbar(it, "OK", duration = SnackbarDuration.Short)
    }

    Scaffold(
        topBar = { TopBar(appName) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
    { innerPadding ->
        val initialized by viewModel.initialized.collectAsStateWithLifecycle()

        if (initialized) {
            AppCalendarStateScreen(viewModel, innerPadding)
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
private fun AppCalendarStateScreen(
    viewModel: TimeEventViewModel,
    innerPadding: PaddingValues
) {
    val calendarState by viewModel.calendarState.collectAsState()

    when (calendarState) {
        State.BEFORE_INITIALIZING -> {
            LoadingScreen(innerPadding)
        }

        State.SUCCESSFULLY_INITIALIZED -> {
            TimeLoggerScreen(viewModel, innerPadding)
        }

        State.CALENDARS_NOT_FOUND -> {
            Box(modifier = Modifier.padding(innerPadding)) {
                Text(
                    text = "Sorry, but no calendars found",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 30.sp
                )
            }
        }
    }
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
fun TimeLoggerScreen(viewModel: TimeEventViewModel, innerPadding: PaddingValues) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> TimeLoggerPortraitScreen(viewModel, innerPadding)
        else -> TimeLoggerLandscapeScreen(viewModel, innerPadding)
    }
}

@Composable
fun TimeLoggerPortraitScreen(viewModel: TimeEventViewModel, innerPadding: PaddingValues) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding),
    ) {
        val calendars by viewModel.availableCalendars.collectAsState()
        val selectedCalendar by viewModel.selectedCalendar.collectAsState()

        CalendarDropdown(calendars, selectedCalendar, { viewModel.selectCalendar(it) })
        TitleAndColorRow(viewModel)

        StartDateTimeRow(viewModel)
        Spacer(Modifier.height(10.dp))
        EndDateTimeRow(viewModel)
        DurationRow(viewModel)
        TimeLoggerButtonsRow(viewModel)

        Spacer(Modifier.height(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(70.dp),
            onClick = { viewModel.trySave() }) {
            Text(text = "Save")
        }
    }
}

@Composable
fun TimeLoggerLandscapeScreen(viewModel: TimeEventViewModel, innerPadding: PaddingValues) {

    Column(
        modifier = Modifier.padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val calendars by viewModel.availableCalendars.collectAsState()
            val selectedCalendar by viewModel.selectedCalendar.collectAsState()

            Box(modifier = Modifier.weight(1.0f)) {
                CalendarDropdown(calendars, selectedCalendar, { viewModel.selectCalendar(it) })
            }
            Box(modifier = Modifier.weight(1.0f)) {
                TitleAndColorRow(viewModel)
            }
        }

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StartDateTimeRow(viewModel)
                Spacer(Modifier.height(10.dp))
                EndDateTimeRow(viewModel)
            }

            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DurationRow(viewModel)
                TimeLoggerButtonsRow(viewModel)

                Spacer(Modifier.height(10.dp))

                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(70.dp),
                    onClick = { viewModel.trySave() }) {
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
private fun TitleAndColorRow(viewModel: TimeEventViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        val entryTitle by viewModel.eventTitle.collectAsState()
        val colors by viewModel.availableColors.collectAsState()
        val selectedColor by viewModel.selectedColor.collectAsState()

        EntryTitleTextField(entryTitle, { viewModel.updateEventTitle(it) })

        ColorDropdown(colors, selectedColor, { viewModel.selectColor(it) })
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
private fun StartDateTimeRow(viewModel: TimeEventViewModel) {
    DateTimePickerRow(
        "Start",
        viewModel.startDateTime.collectAsState().value,
        { localDate -> viewModel.updateStartDate(localDate) },
        { localTime -> viewModel.updateStartTime(localTime) })
}

@Composable
private fun EndDateTimeRow(viewModel: TimeEventViewModel) {
    DateTimePickerRow(
        "End",
        viewModel.endDateTime.collectAsState().value,
        { localDate -> viewModel.updateEndDate(localDate) },
        { localTime -> viewModel.updateEndTime(localTime) })
}

@Composable
private fun DurationRow(viewModel: TimeEventViewModel) {
    Text(
        text = "Duration: ${viewModel.durationStr.collectAsState().value}",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
    )
}

@Composable
private fun TimeLoggerButtonsRow(viewModel: TimeEventViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val timerState by viewModel.timerState.collectAsState()

        val buttonModifier = Modifier
            .weight(0.5f)
            .height(50.dp)

        val buttonElevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { viewModel.reset() },
            elevation = buttonElevation,
            enabled = (timerState == TimerState.READY_TO_START || timerState == TimerState.STOPPED)
        ) {
            Text(text = "Restart")
        }

        Spacer(Modifier.width(16.dp))

        when (timerState) {
            TimerState.READY_TO_START -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { viewModel.start() },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToStartButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Start")
            }

            TimerState.STARTED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { viewModel.stop() },
                elevation = buttonElevation,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TimeLoggerTheme.colors.timerToStopButton,
                    contentColor = TimeLoggerTheme.colors.timerButtonContent
                )
            ) {
                Text(text = "Stop")
            }

            TimerState.STOPPED -> ElevatedButton(
                modifier = buttonModifier,
                onClick = { viewModel.resume() },
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