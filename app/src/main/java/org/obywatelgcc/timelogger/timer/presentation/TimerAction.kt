package org.obywatelgcc.timelogger.timer.presentation

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState.SavingType
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState.Suggestion
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

sealed interface TimerAction {
    data class SelectCalendar(val calendar: Calendar) : TimerAction
    data class SelectColor(val color: CalendarEventColor) : TimerAction

    data class UpdateTitle(val title: String) : TimerAction
    data class SelectSuggestion(val suggestion: Suggestion) : TimerAction
    object StartTimer : TimerAction
    object StopTimer : TimerAction
    object ResumeTimer : TimerAction
    object RestartTimer : TimerAction
    data class UpdateStartDate(val date: LocalDate) : TimerAction
    data class UpdateStartTime(val time: LocalTime) : TimerAction
    data class UpdateEndDate(val date: LocalDate) : TimerAction
    data class UpdateEndTime(val time: LocalTime) : TimerAction
    data class OffsetStartTime(val value: Long, val unit: ChronoUnit) : TimerAction
    data class OffsetEndTime(val value: Long, val unit: ChronoUnit) : TimerAction

    data class UpdateSavingType(val newSavingType: SavingType) : TimerAction

    data object TrySave : TimerAction
}