package org.obywatelgcc.timelogger.timer.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessage
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessageBus
import org.obywatelgcc.timelogger.core.presentation.components.ObserveAsEvents

typealias OnAction = (TimerAction) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimerViewModel) {

    val snackbarMessageBus = koinInject<SnackbarMessageBus>()

    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val titleState by viewModel.titleState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    var showNoCalendarDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.effectsFlow) {
        when (it) {
            is TimerEffect.SavingMessage -> snackbarMessageBus.send(
                SnackbarMessage(it.message, "OK", SnackbarDuration.Short)
            )

            is TimerEffect.ValidationError -> snackbarMessageBus.send(
                SnackbarMessage(it.message, "OK", SnackbarDuration.Short)
            )

            TimerEffect.NoCalendarSelected -> showNoCalendarDialog = true
        }
    }

    if (showNoCalendarDialog) {
        AlertDialog(
            onDismissRequest = { showNoCalendarDialog = false },
            title = { Text("No calendar selected") },
            text = { Text("No calendar has been selected. Please go to Settings and select a calendar.") },
            confirmButton = {
                TextButton(onClick = { showNoCalendarDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()

    if (initialized) {
        TimerMainScreen(timerState, titleState, settingsState, viewModel::onAction)
    } else {
        TimerLoadingScreen()
    }
}



