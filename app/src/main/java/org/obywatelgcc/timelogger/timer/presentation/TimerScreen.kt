package org.obywatelgcc.timelogger.timer.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessage
import org.obywatelgcc.timelogger.core.presentation.SnackbarMessageBus
import org.obywatelgcc.timelogger.core.presentation.components.ObserveAsEvents

typealias OnAction = (TimerAction) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen() {

    val viewModel = koinViewModel<TimerViewModel>()
    val snackbarMessageBus = koinInject<SnackbarMessageBus>()

    val calenderState by viewModel.calendarState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val titleState by viewModel.titleState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()


    ObserveAsEvents(viewModel.effectsFlow) {
        when (it) {
            is TimerEffect.SavingMessage -> snackbarMessageBus.send(
                SnackbarMessage(it.message, "OK", SnackbarDuration.Short)
            )

            is TimerEffect.ValidationError -> snackbarMessageBus.send(
                SnackbarMessage(it.message, "OK", SnackbarDuration.Short)
            )
        }
    }

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()

    if (initialized) {
        TimerMainScreen(calenderState, timerState, titleState, settingsState, viewModel::onAction)
    } else {
        TimerLoadingScreen()
    }
}



