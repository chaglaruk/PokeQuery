package com.example.pokequery.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val AGGRESSIVE_MODE = booleanPreferencesKey("aggressive_mode")
        val EXPERT_MODE = booleanPreferencesKey("expert_mode")
        val FIRST_USE_SEEN = booleanPreferencesKey("first_use_seen")
        val FAVORITES = stringSetPreferencesKey("favorites_set") // stored as JSON strings or raw strings for MVP
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            aggressiveMode = preferences[AGGRESSIVE_MODE] ?: false,
            expertMode = preferences[EXPERT_MODE] ?: false,
            firstUseSeen = preferences[FIRST_USE_SEEN] ?: false,
            favorites = preferences[FAVORITES] ?: emptySet()
        )
    }

    suspend fun setAggressiveMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[AGGRESSIVE_MODE] = enabled }
    }

    suspend fun setExpertMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[EXPERT_MODE] = enabled }
    }

    suspend fun setFirstUseSeen(seen: Boolean) {
        dataStore.edit { prefs -> prefs[FIRST_USE_SEEN] = seen }
    }

    suspend fun addFavorite(query: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES] ?: emptySet()
            prefs[FAVORITES] = current + query
        }
    }

    suspend fun removeFavorite(query: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES] ?: emptySet()
            prefs[FAVORITES] = current - query
        }
    }
}

data class UserPreferences(
    val aggressiveMode: Boolean,
    val expertMode: Boolean,
    val firstUseSeen: Boolean,
    val favorites: Set<String>
)
