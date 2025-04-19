package org.obywatelgcc.timelogger.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.viewmodel.TimeEntryViewModel
import org.obywatelgcc.timelogger.viewmodel.UiState


@Composable
fun App(innerPadding: PaddingValues) {
    val viewModel: TimeEntryViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState()

    when (uiState.value) {
        UiState.INITIALIZING -> {
            CircularProgressIndicator()
        }

        UiState.INITIALIZED -> {
            TimeLoggerScreen(viewModel, innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun TimeLoggerScreen(viewModel: TimeEntryViewModel, innerPadding: PaddingValues) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding)
    ) {

        val calendars = viewModel.availableCalendars.collectAsState()
        val selectedCalendar = viewModel.selectedCalendar.collectAsState()
        val taskDescription = viewModel.taskDescription.collectAsState()

        CalendarDropdown(calendars.value, selectedCalendar.value, { viewModel.selectCalendar(it) })
        TaskDescriptionTextField(taskDescription.value, { viewModel.updateTaskDescription(it) })
        StartDateTimeRow(viewModel)
        EndDateTimeRow(viewModel)
        DurationRow(viewModel)
        TimeLoggerButtonsRow(viewModel)

        Spacer(Modifier.width(20.dp))

        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(50.dp),
            onClick = { viewModel.save() }) {
            Text(text = "Save")
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDropdown(
    calenderas: List<Calendar>,
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
            calenderas.forEach { c ->
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
private fun TaskDescriptionTextField(
    initValue: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = initValue,
        onValueChange = onValueChange,
        label = { Text(text = "Task description") },
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
        val buttonModifier = Modifier
            .weight(1f)
            .height(50.dp)

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { viewModel.reset() }) {
            Text(text = "Restart")
        }

        Spacer(Modifier.width(16.dp))

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { viewModel.start() }) {
            Text(text = "Start")
        }

        Spacer(Modifier.width(16.dp))

        ElevatedButton(
            modifier = buttonModifier,
            onClick = { viewModel.stop() }) {
            Text(text = "Stop")
        }
    }
}