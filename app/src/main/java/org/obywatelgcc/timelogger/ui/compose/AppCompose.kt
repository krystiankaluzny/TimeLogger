package org.obywatelgcc.timelogger.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import org.obywatelgcc.timelogger.viewmodel.State
import org.obywatelgcc.timelogger.viewmodel.TimeEntryViewModel
import org.obywatelgcc.timelogger.viewmodel.TimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: TimeEntryViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val appName = viewModel.appName.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.messageToShow.collect {
            snackbarHostState.showSnackbar(it, "OK", duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = appName.value) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
    { innerPadding ->
        AppCalendarState(viewModel, innerPadding)
    }

}

@Composable
private fun AppCalendarState(
    viewModel: TimeEntryViewModel,
    innerPadding: PaddingValues
) {
    val calendarState = viewModel.calendarState.collectAsState()

    when (calendarState.value) {
        State.BEFORE_INITIALIZING -> {
            CircularProgressIndicator()
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun TimeLoggerScreen(viewModel: TimeEntryViewModel, innerPadding: PaddingValues) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding),
    ) {
        val calendars = viewModel.availableCalendars.collectAsState()
        val selectedCalendar = viewModel.selectedCalendar.collectAsState()
        val entryTitle = viewModel.entryTitle.collectAsState()

        CalendarDropdown(calendars.value, selectedCalendar.value, { viewModel.selectCalendar(it) })
        EntryTitleTextField(entryTitle.value, { viewModel.updateEntryTitle(it) })
        StartDateTimeRow(viewModel)
        EndDateTimeRow(viewModel)
        DurationRow(viewModel)
        TimeLoggerButtonsRow(viewModel)

        Spacer(Modifier.width(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(50.dp),
            onClick = { viewModel.trySave() }) {
            Text(text = "Save")
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
private fun EntryTitleTextField(
    initValue: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = initValue,
        onValueChange = onValueChange,
        label = { Text(text = "Event title") },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun StartDateTimeRow(viewModel: TimeEntryViewModel) {
    DateTimePickerRow(
        "Start",
        viewModel.startDateTime.collectAsState().value,
        { localDate -> viewModel.updateStartDate(localDate) },
        { localTime -> viewModel.updateStartTime(localTime) })
}

@Composable
private fun EndDateTimeRow(viewModel: TimeEntryViewModel) {
    DateTimePickerRow(
        "End",
        viewModel.endDateTime.collectAsState().value,
        { localDate -> viewModel.updateEndDate(localDate) },
        { localTime -> viewModel.updateEndTime(localTime) })
}

@Composable
private fun DurationRow(viewModel: TimeEntryViewModel) {
    Text(
        text = "Duration: ${viewModel.durationStr.collectAsState().value}",
        modifier = Modifier.padding(16.dp),
        fontSize = 20.sp
    )
}

@Composable
private fun TimeLoggerButtonsRow(viewModel: TimeEntryViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        val timerState = viewModel.timerState.collectAsState().value

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