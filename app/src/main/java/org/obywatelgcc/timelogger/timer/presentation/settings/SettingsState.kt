package org.obywatelgcc.timelogger.timer.presentation.settings

import kotlinx.serialization.Serializable

data class SettingsState(
    val savingType: SavingType = SavingType.SaveOnly
) {

    @Serializable
    sealed class SavingType(val description: String) {

        @Serializable
        object SaveOnly : SavingType("Save only")
        @Serializable
        object SaveAndStartNew : SavingType("Save and start new event")
    }
}