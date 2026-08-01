package com.uptbal.sace.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uptbal.sace.data.api.EstudianteDto
import com.uptbal.sace.data.api.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USER = stringPreferencesKey("user")
        val KEY_ESTUDIANTE = stringPreferencesKey("estudiante")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    val user: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER]?.let {
            runCatching { json.decodeFromString<UserDto>(it) }.getOrNull()
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]

    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER] = json.encodeToString(UserDto.serializer(), user)
        }
    }

    suspend fun saveEstudiante(estudiante: EstudianteDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ESTUDIANTE] = json.encodeToString(EstudianteDto.serializer(), estudiante)
        }
    }

    suspend fun getEstudiante(): EstudianteDto? =
        context.dataStore.data.first()[KEY_ESTUDIANTE]?.let {
            runCatching { json.decodeFromString<EstudianteDto>(it) }.getOrNull()
        }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
