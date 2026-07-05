package com.caglar.pokequery.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.caglar.pokequery.data.model.CleaningJournalEntry
import com.caglar.pokequery.data.model.PersonalPreset
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
        val SAFETY_STYLE = stringPreferencesKey("safety_style")
        val GAME_LANGUAGE = stringPreferencesKey("game_language")
        // v0.5.2 (Fix 7): two-layer localization. APP_LANGUAGE controls UI text only;
        // GAME_LANGUAGE controls the generated Pokémon GO search strings only. They are
        // independent — choosing a Turkish UI must NOT force Turkish search strings.
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        // v0.6.1: local-only personal presets and cleaning journal entries. LOCAL ONLY — never
        // synced, never uploaded, never account-bound. Stored via the codecs in UserContentCodec.
        val PERSONAL_PRESETS = stringSetPreferencesKey("personal_presets_v1")
        val JOURNAL_ENTRIES = stringSetPreferencesKey("journal_entries_v1")
        // v0.6.2 polish: clipboard import detection in Explain.
        val CLIPBOARD_DETECTION_ENABLED = booleanPreferencesKey("clipboard_detection_enabled")
        // v0.6.2 polish: expand risk limitations by default.
        val LIMITATIONS_EXPANDED_BY_DEFAULT = booleanPreferencesKey("limitations_expanded_by_default")
        val EVENT_GUIDE_UPDATES_ENABLED = booleanPreferencesKey("event_guide_updates_enabled")
        val EVENT_GUIDE_REFRESH_ON_OPEN = booleanPreferencesKey("event_guide_refresh_on_open")
        val EVENT_GUIDE_PREFER_SAVED_OFFLINE = booleanPreferencesKey("event_guide_prefer_saved_offline")
        val EVENT_GUIDE_SHOW_PLANNING_HINTS = booleanPreferencesKey("event_guide_show_planning_hints")
        val EXTRA_ACTION_SAFETY_WARNINGS = booleanPreferencesKey("extra_action_safety_warnings")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            firstUseSeen = preferences[FIRST_USE_SEEN] ?: false,
            warningBehavior = preferences[WARNING_BEHAVIOR] ?: "Always Warn",
            safetyStyle = preferences[SAFETY_STYLE] ?: "Conservative",
            gameLanguage = preferences[GAME_LANGUAGE] ?: "Auto",
            appLanguage = preferences[APP_LANGUAGE] ?: "System Default",
            clipboardDetectionEnabled = preferences[CLIPBOARD_DETECTION_ENABLED] ?: true,
            limitationsExpandedByDefault = preferences[LIMITATIONS_EXPANDED_BY_DEFAULT] ?: false,
            eventGuideRefreshOnOpen = preferences[EVENT_GUIDE_REFRESH_ON_OPEN] ?: true,
            eventGuidePreferSavedOffline = preferences[EVENT_GUIDE_PREFER_SAVED_OFFLINE] ?: true,
            eventGuideShowPlanningHints = preferences[EVENT_GUIDE_SHOW_PLANNING_HINTS] ?: true,
            extraActionSafetyWarnings = preferences[EXTRA_ACTION_SAFETY_WARNINGS] ?: true,
            favorites = readFavorites(preferences).sortedByDescending { it.createdAt },
            history = readHistory(preferences).sortedByDescending { it.createdAt },
            personalPresets = readPersonalPresets(preferences).sortedByDescending { it.updatedAt },
            journal = readJournal(preferences).sortedByDescending { it.updatedAt }
        )
    }

    suspend fun setSetting(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }

    suspend fun setFirstUseSeen(seen: Boolean) {
        dataStore.edit { it[FIRST_USE_SEEN] = seen }
    }

    suspend fun setClipboardDetectionEnabled(enabled: Boolean) {
        dataStore.edit { it[CLIPBOARD_DETECTION_ENABLED] = enabled }
    }

    suspend fun setLimitationsExpandedByDefault(enabled: Boolean) {
        dataStore.edit { it[LIMITATIONS_EXPANDED_BY_DEFAULT] = enabled }
    }

    suspend fun setBooleanSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { it[key] = value }
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

    // ---- v0.6.1: Personal Presets (local only) ----
    /** Adds a personal preset. Duplicate handling: if a preset with the same query already exists,
     *  it is replaced by this one (no endless duplicates). */
    suspend fun addPersonalPreset(preset: PersonalPreset) {
        dataStore.edit { preferences ->
            val presets = readPersonalPresets(preferences)
                .filterNot { it.queryString == preset.queryString } + preset
            preferences[PERSONAL_PRESETS] = presets.map(PersonalPresetCodec::encode).toSet()
        }
    }

    suspend fun renamePersonalPreset(id: String, newTitle: String) {
        dataStore.edit { preferences ->
            val presets = readPersonalPresets(preferences).map {
                if (it.id == id) it.copy(title = newTitle, updatedAt = System.currentTimeMillis()) else it
            }
            preferences[PERSONAL_PRESETS] = presets.map(PersonalPresetCodec::encode).toSet()
        }
    }

    suspend fun removePersonalPreset(id: String) {
        dataStore.edit { preferences ->
            preferences[PERSONAL_PRESETS] = readPersonalPresets(preferences)
                .filterNot { it.id == id }
                .map(PersonalPresetCodec::encode)
                .toSet()
        }
    }

    suspend fun clearPersonalPresets() {
        dataStore.edit { it.remove(PERSONAL_PRESETS) }
    }

    fun hasPersonalPresetForQuery(preferences: Preferences, query: String): Boolean =
        readPersonalPresets(preferences).any { it.queryString == query }

    // ---- v0.6.1: Cleaning Journal (local only, user-entered) ----
    suspend fun addJournal(entry: CleaningJournalEntry) {
        dataStore.edit { preferences ->
            val entries = (listOf(entry) + readJournal(preferences).filterNot { it.id == entry.id })
            preferences[JOURNAL_ENTRIES] = entries.map(JournalCodec::encode).toSet()
        }
    }

    suspend fun updateJournal(entry: CleaningJournalEntry) {
        dataStore.edit { preferences ->
            val entries = readJournal(preferences).map {
                if (it.id == entry.id) entry.copy(updatedAt = System.currentTimeMillis()) else it
            }
            preferences[JOURNAL_ENTRIES] = entries.map(JournalCodec::encode).toSet()
        }
    }

    suspend fun removeJournal(id: String) {
        dataStore.edit { preferences ->
            preferences[JOURNAL_ENTRIES] = readJournal(preferences)
                .filterNot { it.id == id }
                .map(JournalCodec::encode)
                .toSet()
        }
    }

    suspend fun clearJournal() {
        dataStore.edit { it.remove(JOURNAL_ENTRIES) }
    }

    suspend fun resetSettings() {
        dataStore.edit { preferences ->
            val favs = preferences[SAVED_TEMPLATES]
            val history = preferences[RECENT_HISTORY]
            val presets = preferences[PERSONAL_PRESETS]
            val journal = preferences[JOURNAL_ENTRIES]
            val firstUse = preferences[FIRST_USE_SEEN]
            preferences.clear()
            if (favs != null) preferences[SAVED_TEMPLATES] = favs
            if (history != null) preferences[RECENT_HISTORY] = history
            if (presets != null) preferences[PERSONAL_PRESETS] = presets
            if (journal != null) preferences[JOURNAL_ENTRIES] = journal
            if (firstUse != null) preferences[FIRST_USE_SEEN] = firstUse
        }
    }

    private fun readPersonalPresets(preferences: Preferences): List<PersonalPreset> =
        preferences[PERSONAL_PRESETS].orEmpty().mapNotNull(PersonalPresetCodec::decode)

    private fun readJournal(preferences: Preferences): List<CleaningJournalEntry> =
        preferences[JOURNAL_ENTRIES].orEmpty().mapNotNull(JournalCodec::decode)

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
    val safetyStyle: String = "Conservative",
    val gameLanguage: String = "Auto",
    val appLanguage: String = "System Default",
    val clipboardDetectionEnabled: Boolean = true,
    val limitationsExpandedByDefault: Boolean = false,
    val eventGuideRefreshOnOpen: Boolean = true,
    val eventGuidePreferSavedOffline: Boolean = true,
    val eventGuideShowPlanningHints: Boolean = true,
    val extraActionSafetyWarnings: Boolean = true,
    val favorites: List<SavedTemplate>,
    val history: List<SavedTemplate> = emptyList(),
    val personalPresets: List<PersonalPreset> = emptyList(),
    val journal: List<CleaningJournalEntry> = emptyList()
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
