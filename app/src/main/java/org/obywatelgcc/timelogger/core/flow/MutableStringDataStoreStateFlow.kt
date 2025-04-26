package org.obywatelgcc.timelogger.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager

class MutableStringDataStoreStateFlow(
    private val coroutineScope: CoroutineScope,
    private val dataStoreManager: DataStoreManager,
    private val key: String,
    private val defaultValue: String,

    private val _state: MutableStateFlow<String> = MutableStateFlow(defaultValue)

) : Flow<String> by _state {
    var value: String
        get() = _state.value
        set(value) {
            _state.value = value
            coroutineScope.launch {
                dataStoreManager.saveString(key, value)
            }
        }

    suspend fun loadFormDataStore() {
        _state.value = dataStoreManager.getString(key, defaultValue).first()
    }

    fun asStateFlow(): StateFlow<String> = _state.asStateFlow()
}