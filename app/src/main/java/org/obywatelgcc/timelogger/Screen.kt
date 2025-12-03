package org.obywatelgcc.timelogger

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    object TimeLogger : Screen()

    @Serializable
    object Statistics : Screen()

    @Serializable
    object Categories : Screen()

    @Serializable
    object Settings : Screen()
}