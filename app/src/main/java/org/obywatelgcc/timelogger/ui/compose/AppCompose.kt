package org.obywatelgcc.timelogger.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.obywatelgcc.timelogger.viewmodel.TimeEntryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun App(innerPadding: PaddingValues) {
    val viewModel: TimeEntryViewModel = koinViewModel()

    TimeLoggerScreen(viewModel, innerPadding)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun TimeLoggerScreen(viewModel: TimeEntryViewModel, innerPadding: PaddingValues) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding)
    ) {

        var entryDescription by remember { mutableStateOf("") }

        CalendarDropdown()

        TaskDescriptionTextField(entryDescription, { entryDescription = it })

        DateTimePickerRow(
            "Start",
            viewModel.startDateTime.collectAsState().value,
            { localDate -> viewModel.updateStartDate(localDate) },
            { localTime -> viewModel.updateStartTime(localTime) })

        DateTimePickerRow(
            "End",
            viewModel.endDateTime.collectAsState().value,
            { localDate -> viewModel.updateEndDate(localDate) },
            { localTime -> viewModel.updateEndTime(localTime) })

        Text(
            text = "Duration: ${viewModel.durationStr.collectAsState().value}",
            Modifier.padding(16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)
        ) {
            Button(onClick = { viewModel.reset() }) {
                Text(text = "Restart")
            }
            Button(onClick = { viewModel.start() }) {
                Text(text = "Start")
            }
            Button(onClick = { viewModel.stop() }) {
                Text(text = "Stop")
            }
        } // end row

        Button(onClick = { viewModel.save(entryDescription) }) {
            Text(text = "Save")
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
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("A", "B", "C", "D", "E", "F")
    var textFieldState by remember { mutableStateOf(items[0]) }


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
            items.forEach { s ->
                DropdownMenuItem(onClick = {
                    textFieldState = s
                    expanded = false
                }, text = {
                    Text(text = s)
                })
            }
        }
    }
}

@Composable
fun DateTimePickerRow(
    title: String,
    initTimeDate: LocalDateTime,
    onDateSelected: (LocalDate) -> Unit = {},
    onTimeSelected: (LocalTime) -> Unit = {}
) {

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss")
    val initDate = initTimeDate.toLocalDate()
    val initTime = initTimeDate.toLocalTime()

    ElevatedCard(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 10.dp, top = 5.dp),
            fontSize = LocalTextStyle.current.fontSize / 1.3
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
        ) {
            TextFiledWithPicker(
                initDate.format(dateFormatter), trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }, modifier = Modifier.weight(1f)
            ) { onDismiss ->
                DatePickerDialog(onDateSelected = onDateSelected, onDismiss = onDismiss)
            }

            Spacer(Modifier.width(16.dp))

            TextFiledWithPicker(
                initTime.format(timeFormatter), modifier = Modifier.weight(1f)
            ) { onDismiss ->
                TimePickerDialog(
                    initTime = initTime, onTimeSelected = onTimeSelected, onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun TextFiledWithPicker(
    value: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    picker: @Composable (onDismiss: () -> Unit) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        trailingIcon = trailingIcon,
        modifier = modifier.pointerInput(value) {
            awaitEachGesture {
                // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                // in the Initial pass to observe events before the text field consumes them
                // in the Main pass.
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                    showPicker = true
                }
            }
        },
    )

    if (showPicker) {
        picker { showPicker = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val localDate = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            onDateSelected(localDate)
            onDismiss()
        }) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initTime.hour,
        initialMinute = initTime.minute,
        is24Hour = true,
    )

    AlertDialog(onDismissRequest = onDismiss, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }, confirmButton = {
        TextButton(onClick = {
            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            onDismiss()
        }) {
            Text("OK")
        }
    }, text = {
        TimePicker(state = timePickerState)
    })
}