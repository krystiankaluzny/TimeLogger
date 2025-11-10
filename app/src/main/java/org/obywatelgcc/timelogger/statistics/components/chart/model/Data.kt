package org.obywatelgcc.timelogger.statistics.components.chart.model

import androidx.compose.ui.graphics.Color

data class Data<T>(
    val value: T,
    val label: String,
    val color: Color = Color.Black
)

