package org.obywatelgcc.timelogger.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import kotlin.reflect.KType

class MutableJsonDataStoreStateFlow<T>(
    private val coroutineScope: CoroutineScope,
    private val dataStoreManager: DataStoreManager,
    private val key: String,
    private val defaultValue: T,
    private val type: KType,
    private val kSerializer: KSerializer<T> = Json.serializersModule.serializer(type) as KSerializer<T>,

    private val _state: MutableStateFlow<T> = MutableStateFlow<T>(defaultValue)

) : Flow<T> by _state {
    var value: T
        get() = _state.value
        set(value) {
            _state.value = value
            coroutineScope.launch {
                dataStoreManager.saveAsJson(key, value, kSerializer)
            }
        }

    suspend fun loadFormDataStore() {
        _state.value = dataStoreManager.getFromJson<T?>(key, kSerializer).first() ?: defaultValue
    }

    fun edit(map: suspend (T) -> T) {
        coroutineScope.launch {
            val newValue = map(_state.value)
            _state.value = newValue
            dataStoreManager.saveAsJson(key, newValue, kSerializer)
        }
    }

    fun asStateFlow(): StateFlow<T> = _state.asStateFlow()
}