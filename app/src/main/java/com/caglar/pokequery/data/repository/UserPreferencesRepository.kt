package com.caglar.pokequery.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.data.model.SavedTemplate
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val FIRST_USE_SEEN = booleanPreferencesKey("first_use_seen")
        val LEGACY_FAVORITES = stringSetPreferencesKey("favorites_set")
        val SAVED_TEMPLATES = stringSetPreferencesKey("saved_templates_v1")
        val RECENT_HISTORY = stringSetPreferencesKey("recent_history_v1")
        val WARNING_BEHAVIOR = stringPreferencesKey("warning_behavior")
        val DUPLICATE_THRESHOLD = stringPreferencesKey("duplicate_threshold")
        val SAFETY_STYLE = stringPreferencesKey("safety_style")
        val COPY_BEHAVIOR = stringPreferencesKey("copy_behavior")
        val GAME_LANGUAGE = stringPreferencesKey("game_language")
        val VISUAL_DENSITY = stringPreferencesKey("visual_density")
        // v0.5.2 (Fix 7): two-layer localization. APP_LANGUAGE controls UI text only;
        // GAME_LANGUAGE controls the generated Pokémon GO search strings only. They are
        // independent — choosing a Turkish UI must NOT force Turkish search strings.
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            firstUseSeen = preferences[FIRST_USE_SEEN] ?: false,
            warningBehavior = preferences[WARNING_BEHAVIOR] ?: "Always Warn",
            duplicateThreshold = preferences[DUPLICATE_THRESHOLD] ?: "Count 3 (Safe)",
            safetyStyle = preferences[SAFETY_STYLE] ?: "Conservative",
            copyBehavior = preferences[COPY_BEHAVIOR] ?: "Confirm Risky Copy",
            gameLanguage = preferences[GAME_LANGUAGE] ?: "English",
            visualDensity = preferences[VISUAL_DENSITY] ?: "Comfortable",
            // v0.5.2 (Fix 7): System Default = follow device locale for UI; empty/legacy
            // values default to System Default (not Turkish) so we never surprise the user.
            appLanguage = preferences[APP_LANGUAGE] ?: "System Default",
            favorites = readFavorites(preferences).sortedByDescending { it.createdAt },
            history = readHistory(preferences).sortedByDescending { it.createdAt }
        )
    }

    suspend fun setSetting(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }

    suspend fun setFirstUseSeen(seen: Boolean) {
        dataStore.edit { it[FIRST_USE_SEEN] = seen }
    }

    suspend fun addFavorite(template: SavedTemplate) {
        dataStore.edit { preferences ->
            val favorites = readFavorites(preferences).filterNot { it.rawSyntax == template.rawSyntax } + template
            preferences[SAVED_TEMPLATES] = favorites.map(SavedTemplateCodec::encode).toSet()
            preferences.remove(LEGACY_FAVORITES)
        }
    }

    suspend fun removeFavorite(id: String) {
        dataStore.edit { preferences ->
            preferences[SAVED_TEMPLATES] = readFavorites(preferences)
                .filterNot { it.id == id }
                .map(SavedTemplateCodec::encode)
                .toSet()
            preferences.remove(LEGACY_FAVORITES)
        }
    }

    suspend fun clearFavorites() {
        dataStore.edit { preferences ->
            preferences.remove(SAVED_TEMPLATES)
            preferences.remove(LEGACY_FAVORITES)
        }
    }

    suspend fun addHistory(template: SavedTemplate) {
        dataStore.edit { preferences ->
            val recent = (listOf(template) + readHistory(preferences).filterNot { it.rawSyntax == template.rawSyntax })
                .take(25)
            preferences[RECENT_HISTORY] = recent.map(SavedTemplateCodec::encode).toSet()
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences.remove(RECENT_HISTORY)
        }
    }

    suspend fun resetSettings() {
        dataStore.edit { preferences ->
            val favs = preferences[SAVED_TEMPLATES]
            val history = preferences[RECENT_HISTORY]
            val firstUse = preferences[FIRST_USE_SEEN]
            preferences.clear()
            if (favs != null) preferences[SAVED_TEMPLATES] = favs
            if (history != null) preferences[RECENT_HISTORY] = history
            if (firstUse != null) preferences[FIRST_USE_SEEN] = firstUse
        }
    }

    private fun readFavorites(preferences: Preferences): List<SavedTemplate> {
        val saved = preferences[SAVED_TEMPLATES].orEmpty().mapNotNull(SavedTemplateCodec::decode)
        val legacy = preferences[LEGACY_FAVORITES].orEmpty().map { raw ->
            SavedTemplate(
                id = UUID.nameUUIDFromBytes(raw.toByteArray(StandardCharsets.UTF_8)).toString(),
                name = "Imported favorite",
                rawSyntax = raw,
                goalId = "legacy",
                riskLevel = RiskLevel.Medium,
                createdAt = 0L
            )
        }
        return (saved + legacy).distinctBy { it.id }
    }

    private fun readHistory(preferences: Preferences): List<SavedTemplate> =
        preferences[RECENT_HISTORY].orEmpty().mapNotNull(SavedTemplateCodec::decode)
}

data class UserPreferences(
    val firstUseSeen: Boolean,
    val warningBehavior: String = "Always Warn",
    val duplicateThreshold: String = "Count 3 (Safe)",
    val safetyStyle: String = "Conservative",
    val copyBehavior: String = "Confirm Risky Copy",
    val gameLanguage: String = "English",
    val visualDensity: String = "Comfortable",
    // v0.5.2 (Fix 7): App UI language, independent from the search-string language.
    val appLanguage: String = "System Default",
    val favorites: List<SavedTemplate>,
    val history: List<SavedTemplate> = emptyList()
)

object SavedTemplateCodec {
    fun encode(template: SavedTemplate): String = listOf(
        template.id,
        template.name,
        template.rawSyntax,
        template.goalId,
        template.riskLevel.name,
        template.createdAt.toString()
    ).joinToString("") { "${it.length}:$it" }

    fun decode(value: String): SavedTemplate? = runCatching {
        var offset = 0
        val fields = List(6) {
            val separator = value.indexOf(':', offset)
            require(separator >= offset)
            val length = value.substring(offset, separator).toInt()
            val start = separator + 1
            val end = start + length
            require(end <= value.length)
            offset = end
            value.substring(start, end)
        }
        require(offset == value.length)
        SavedTemplate(
            id = fields[0],
            name = fields[1],
            rawSyntax = fields[2],
            goalId = fields[3],
            riskLevel = RiskLevel.valueOf(fields[4]),
            createdAt = fields[5].toLong()
        )
    }.getOrNull()
}
