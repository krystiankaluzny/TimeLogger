package org.obywatelgcc.timelogger.core.presentation.components.filter

import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeType

sealed interface FilterTimeRangeAction {
    data class SelectTimeRangeType(val type: FilterTimeRangeType) : FilterTimeRangeAction
    data object PreviousRange : FilterTimeRangeAction
    data object NextRange : FilterTimeRangeAction
    data object ResetRange : FilterTimeRangeAction
}