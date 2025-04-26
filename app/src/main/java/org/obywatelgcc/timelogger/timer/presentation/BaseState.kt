package org.obywatelgcc.timelogger.timer.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.flow.MutableJsonDataStoreStateFlow
import org.obywatelgcc.timelogger.core.flow.MutableSaveStateFlow
import org.obywatelgcc.timelogger.core.flow.MutableStringDataStoreStateFlow
import kotlin.reflect.typeOf

open class BaseState(
    val coroutineScope: CoroutineScope,
    val savedStateHandle: SavedStateHandle,
    val dataStoreManager: DataStoreManager
) {

    inline fun <reified T> jsonDataStoreStateFlow(
        key: String,
        default: T
    ): MutableJsonDataStoreStateFlow<T> =
        MutableJsonDataStoreStateFlow<T>(
            coroutineScope,
            dataStoreManager,
            key,
            default,
            typeOf<T>()
        )

    fun stringDataStoreStateFlow(key: String, default: String): MutableStringDataStoreStateFlow =
        MutableStringDataStoreStateFlow(
            coroutineScope,
            dataStoreManager,
            key,
            default
        )

    fun <T> saveStateFlow(key: String, default: T): MutableSaveStateFlow<T> =
        MutableSaveStateFlow(savedStateHandle, key, default)
}