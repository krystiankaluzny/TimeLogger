package org.obywatelgcc.timelogger.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class DataStoreManager(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appDataStore")
        private val JSON = Json.Default
        private val STRING_KEYS = mutableMapOf<String, Preferences.Key<String>>()
    }

    fun <T> getFromJson(key: String, type: KType): Flow<T?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[getStringKey(key)]?.let {
                    val deserializer =
                        JSON.serializersModule.serializer(type) as DeserializationStrategy<T>
                    var obj: T? = null
                    try {
                        obj = JSON.decodeFromString<T>(deserializer, it)
                    } catch (ex: SerializationException) {
                    }

                    obj
                }
            }
    }

    fun <T> getFromJson(key: String, deserializer: DeserializationStrategy<T>): Flow<T?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[getStringKey(key)]?.let {
                    var obj: T? = null
                    try {
                        obj = JSON.decodeFromString<T>(deserializer, it)
                    } catch (ex: SerializationException) {
                    }

                    obj
                }
            }
    }

    fun getString(key: String, default: String = ""): Flow<String> {
        return context.dataStore.data
            .map { preferences -> preferences[getStringKey(key)] ?: default }
    }

    suspend fun saveAsJson(key: String, value: Any, type: KType) {
        context.dataStore.edit { preferences ->
            val serializer = JSON.serializersModule.serializer(type)
            val jsonStr = JSON.encodeToString(serializer, value)
            preferences[getStringKey(key)] = jsonStr
        }
    }

    suspend fun <T> saveAsJson(key: String, value: T, serializer: SerializationStrategy<T>) {
        context.dataStore.edit { preferences ->
            val jsonStr = JSON.encodeToString(serializer, value)
            preferences[getStringKey(key)] = jsonStr
        }
    }

    suspend fun saveString(key: String, value: String) {
        context.dataStore.edit { preferences ->
            preferences[getStringKey(key)] = value
        }
    }

    private fun getStringKey(key: String): Preferences.Key<String> {
        return STRING_KEYS.getOrPut(key) { stringPreferencesKey(key) }
    }
}