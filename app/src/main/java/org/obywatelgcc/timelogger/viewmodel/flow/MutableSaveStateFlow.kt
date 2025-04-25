package org.obywatelgcc.timelogger.viewmodel.flow

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MutableSaveStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    defaultValue: T,
    private val _state: MutableStateFlow<T> =
        MutableStateFlow(savedStateHandle.get<T>(key) ?: defaultValue)
) : Flow<T> by _state {
    var value: T
        get() = _state.value
        set(value) {
            _state.value = value
            savedStateHandle.set(key, value)
        }

    fun asStateFlow(): StateFlow<T> = _state.asStateFlow()
}