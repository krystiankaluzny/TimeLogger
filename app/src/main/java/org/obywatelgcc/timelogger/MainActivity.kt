package org.obywatelgcc.timelogger

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import org.obywatelgcc.timelogger.viewmodel.TimeEntryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeLoggerTheme {
                Surface() {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    var viewModel: TimeEntryViewModel = viewModel()

    MainScreen(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun MainScreen(viewModel: TimeEntryViewModel) {
    var entryDescription by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
    ) {
        LaunchedEffect(key1 = viewModel.started) {
            while (viewModel.started) {
                delay(1000)
                viewModel.update()
            }
        }

        Spacer(Modifier.width(16.dp))

        OutlinedTextField(
            value = entryDescription,
            onValueChange = { entryDescription = it },
            label = { Text(text = "Some description") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        DateTimePickerRow(
            "Start",
            viewModel.startDateTime.toLocalDate(),
            viewModel.startDateTime.toLocalTime(),
            { localDate -> viewModel.updateStartDate(localDate) },
            { localTime -> viewModel.updateStartTime(localTime) })

        DateTimePickerRow(
            "End",
            viewModel.endDateTime.toLocalDate(),
            viewModel.endDateTime.toLocalTime(),
            { localDate -> viewModel.updateEndDate(localDate) },
            { localTime -> viewModel.updateEndTime(localTime) })

        val durationSeconds = viewModel.duration.seconds
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60
        Text(
            text = "Duration: ${
                String.format(
                    "%d:%02d:%02d", hoursPart, minutesPart, secondsPart
                )
            }", Modifier.padding(16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)
        ) {
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
fun DateTimePickerRow(
    title: String,
    initDate: LocalDate,
    initTime: LocalTime,
    onDateSelected: (LocalDate) -> Unit = {},
    onTimeSelected: (LocalTime) -> Unit = {}
) {

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss")

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
                DatePickerModal(onDateSelected = onDateSelected, onDismiss = onDismiss)
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
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
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
fun DatePickerModal(
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