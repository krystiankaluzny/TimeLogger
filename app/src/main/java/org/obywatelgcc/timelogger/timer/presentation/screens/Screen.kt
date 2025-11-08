package org.obywatelgcc.timelogger.timer.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    object TimeLogger : Screen()

    @Serializable
    object Statistics : Screen()
}