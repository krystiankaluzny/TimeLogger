package org.obywatelgcc.timelogger.timer.compose

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun DateTimePickerRow(
    title: String,
    initTimeDate: LocalDateTime,
    onDateSelected: (LocalDate) -> Unit = {},
    onTimeSelected: (LocalTime) -> Unit = {}
) {

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val initDate = initTimeDate.toLocalDate()
    val initTime = initTimeDate.toLocalTime()

    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 10.dp)
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
                value = initDate.format(dateFormatter),
                modifier = Modifier.weight(4f),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                },
            ) { onDismiss ->
                DatePickerDialog(onDateSelected = onDateSelected, onDismiss = onDismiss)
            }

            Spacer(Modifier.width(16.dp))

            TextFiledWithPicker(
                value = initTime.format(timeFormatter),
                modifier = Modifier.weight(3f),
                trailingIcon = {
                    Icon(Icons.Default.Clock, contentDescription = "Select time")
                },
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

    CustomOutlinedTextField(
        value = value,
        onValueChange = {},
        trailingIcon = trailingIcon,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 0.dp),
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
    val configuration = LocalConfiguration.current

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val localDate = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0)
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                onDateSelected(localDate)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                DatePicker(state = datePickerState)
            }

            else -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = false)
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
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

    val configuration = LocalConfiguration.current
    val usePlatformDefaultWidth: Boolean = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> true
        else -> false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = usePlatformDefaultWidth),
        dismissButton = {
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
        }
    )
}