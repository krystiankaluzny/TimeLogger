package org.obywatelgcc.timelogger.timer.presentation

import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEventColor
import java.time.LocalDate
import java.time.LocalTime

sealed interface TimerAction {
    data class SelectCalendar(val calendar: Calendar) : TimerAction
    data class SelectColor(val color: CalendarEventColor) : TimerAction

    data class UpdateTitle(val title: String) : TimerAction
    object StartTimer : TimerAction
    object StopTimer : TimerAction
    object ResumeTimer : TimerAction
    object RestartTimer : TimerAction
    data class UpdateStartDate(val date: LocalDate) : TimerAction
    data class UpdateStartTime(val time: LocalTime) : TimerAction
    data class UpdateEndDate(val date: LocalDate) : TimerAction
    data class UpdateEndTime(val time: LocalTime) : TimerAction

    data object TrySave : TimerAction
}