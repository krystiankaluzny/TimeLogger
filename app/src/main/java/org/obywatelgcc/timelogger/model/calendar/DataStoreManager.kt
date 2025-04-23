package org.obywatelgcc.timelogger.model.calendar

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class DataStoreManager(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appDataStore")
        private val JSON = Json
    }

    fun <T : Any> getFromJson(key: String, type: KType): Flow<T?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[stringPreferencesKey(key)]?.let {
                    val deserializer =
                        JSON.serializersModule.serializer(type) as DeserializationStrategy<T>
                    var obj: T? = null
                    try {
                        obj = JSON.decodeFromString<T>(deserializer, it)
                    } catch (ex: kotlinx.serialization.SerializationException) {
                    }

                    obj
                }
            }
    }

    suspend fun saveAsJson(key: String, value: Any, type: KType) {
        context.dataStore.edit { preferences ->
            val deserializer = JSON.serializersModule.serializer(type)
            val jsonStr = JSON.encodeToString(deserializer, value)
            preferences[stringPreferencesKey(key)] = jsonStr
        }
    }
}