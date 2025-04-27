package org.obywatelgcc.timelogger

import androidx.compose.runtime.Composable
import org.obywatelgcc.timelogger.timer.presentation.RootTimerScreen
import org.obywatelgcc.timelogger.timer.presentation.TimerViewModel

@Composable
fun MainScreen(viewModel: TimerViewModel) {
    RootTimerScreen(viewModel)
}