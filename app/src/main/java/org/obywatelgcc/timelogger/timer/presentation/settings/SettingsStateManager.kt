package org.obywatelgcc.timelogger.timer.presentation.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState.SavingType
import org.obywatelgcc.timelogger.utils.logInfo

class SettingsStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: SettingsState
) : BaseStateManager<SettingsState>(coroutineScope, savedStateHandle, dataStoreManager, initialState) {

    private val settingsPreferences =
        jsonDataStoreStateFlow("SettingsPreferences_v2", SettingsPreferences())

    suspend fun init() {
        logInfo("init")
        settingsPreferences.loadFormDataStore()

        state.update {
            it.copy(
                savingType = settingsPreferences.value.savingType
            )
        }
    }

    fun updateSavingType(newSavingType: SavingType) {
        if (newSavingType != state.value.savingType) {
            state.update { it.copy(savingType = newSavingType) }
            settingsPreferences.edit { preferences -> preferences.copy(savingType = newSavingType) }
        }
    }

}


@Serializable
data class SettingsPreferences(
    var savingType: SavingType = SavingType.SaveOnly
)