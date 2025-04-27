package org.obywatelgcc.timelogger.timer.presentation

sealed interface TimerEffect {
    data class ValidationError(val message: String) : TimerEffect
    data class SavingMessage(val message: String) : TimerEffect
}