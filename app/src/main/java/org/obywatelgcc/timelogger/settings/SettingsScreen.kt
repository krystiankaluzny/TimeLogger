package org.obywatelgcc.timelogger.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.LocalTime

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val sleepWindowSettings by viewModel.sleepWindowSettings.collectAsStateWithLifecycle()
    val otherSettings by viewModel.otherSettings.collectAsStateWithLifecycle()
    val calendarSettings by viewModel.calendarSettings.collectAsStateWithLifecycle()
    val availableCalendars by viewModel.availableCalendars.collectAsStateWithLifecycle()
    val initialized by viewModel.initialized.collectAsStateWithLifecycle()

    if (initialized) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Calendar",
                style = TimeLoggerTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            CalendarDropdown(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                calendars = availableCalendars.allCalendars,
                selected = calendarSettings.selectedCalendar,
                onSelect = { viewModel.onAction(SettingsAction.SelectCalendar(it)) }
            )

            Text(
                text = "Sleep",
                style = TimeLoggerTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            SleepEnabledRow(
                enabled = sleepWindowSettings.enabled,
                onToggle = { viewModel.onAction(SettingsAction.SleepWindowEnabled(it)) }
            )

            if (sleepWindowSettings.enabled) {
                TimePickerRow(
                    label = "Bedtime",
                    time = sleepWindowSettings.start,
                    onTimeSelected = { viewModel.onAction(SettingsAction.SleepWindowStart(it)) }
                )
                TimePickerRow(
                    label = "Wake up time",
                    time = sleepWindowSettings.end,
                    onTimeSelected = { viewModel.onAction(SettingsAction.SleepWindowEnd(it)) }
                )
            }

            Text(
                text = "Others",
                style = TimeLoggerTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )

            CountUnmeasuredGapsRow(
                enabled = otherSettings.countUnmeasuredGaps,
                onToggle = { viewModel.onAction(SettingsAction.CountUnmeasuredGapsEnabled(it)) }
            )
        }
    }

}

@Composable
private fun SleepEnabledRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Track sleep time",
            modifier = Modifier.weight(1f),
            style = TimeLoggerTheme.typography.bodyLarge
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(
    label: String,
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TimeLoggerTheme.typography.bodyLarge
        )
        Text(
            text = String.format("%02d:%02d", time.hour, time.minute),
            style = TimeLoggerTheme.typography.bodyLarge
        )
    }

    if (showDialog) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime.of(state.hour, state.minute))
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            text = { TimePicker(state = state) }
        )
    }
}

@Composable
private fun CountUnmeasuredGapsRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Count unmeasured gaps",
            modifier = Modifier.weight(1f),
            style = TimeLoggerTheme.typography.bodyLarge
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}