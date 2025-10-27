package org.obywatelgcc.timelogger.timer.presentation.timer

import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

data class TimerState(
    val runningState: RunningState = RunningState.READY_TO_START,
    val savingType: SavingType = SavingType.SAVE_ONLY,
    val eventTitle: String = "",
    val startDateTime: LocalDateTime = LocalDateTime.MIN,
    val endDateTime: LocalDateTime = LocalDateTime.MIN,
) {

    val durationStr: String by lazy {
        val durationSeconds = Duration.between(startDateTime, endDateTime).seconds
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60

        String.format(
            Locale.getDefault(Locale.Category.FORMAT),
            "%d:%02d:%02d",
            hoursPart,
            minutesPart,
            secondsPart
        )
    }

    enum class RunningState {
        READY_TO_START, STARTED, STOPPED
    }

    enum class SavingType {
        SAVE_ONLY, SAVE_AND_START, SAVE_START_AND_CHANGE_COLOR
    }

}
