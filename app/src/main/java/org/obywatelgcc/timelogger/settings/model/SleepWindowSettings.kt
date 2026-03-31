package org.obywatelgcc.timelogger.settings.model

import java.time.LocalTime

data class SleepWindowSettings(
    val enabled: Boolean = false,
    val start: LocalTime = LocalTime.of(22, 0),
    val end: LocalTime = LocalTime.of(10, 0)
)