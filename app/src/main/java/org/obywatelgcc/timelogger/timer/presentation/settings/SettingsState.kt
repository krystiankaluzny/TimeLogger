package org.obywatelgcc.timelogger.timer.presentation.settings

data class SettingsState(
    val savingType: SavingType = SavingType.SAVE_ONLY
) {

    enum class SavingType {
        SAVE_ONLY, SAVE_AND_START, SAVE_START_AND_CHANGE_COLOR
    }
}