package org.obywatelgcc.timelogger.timer.presentation.timer

import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.absoluteValue

data class TimerState(
    val runningState: RunningState = RunningState.READY_TO_START,
    val startDateTime: LocalDateTime = LocalDateTime.MIN,
    val endDateTime: LocalDateTime = LocalDateTime.MIN,
) {

    val durationStr: String by lazy {
        val duration = Duration.between(startDateTime, endDateTime)
        val durationSeconds = duration.seconds.absoluteValue
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60

        String.format(
            Locale.getDefault(Locale.Category.FORMAT),
            "%s%d:%02d:%02d",
            if (duration.isNegative) "-" else "",
            hoursPart,
            minutesPart,
            secondsPart
        )
    }

    enum class RunningState {
        READY_TO_START, STARTED, STOPPED
    }
}
