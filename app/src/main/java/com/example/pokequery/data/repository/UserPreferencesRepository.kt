package com.example.pokequery.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.data.model.SavedTemplate
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val FIRST_USE_SEEN = booleanPreferencesKey("first_use_seen")
        val LEGACY_FAVORITES = stringSetPreferencesKey("favorites_set")
        val SAVED_TEMPLATES = stringSetPreferencesKey("saved_templates_v1")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            firstUseSeen = preferences[FIRST_USE_SEEN] ?: false,
            favorites = readFavorites(preferences).sortedByDescending { it.createdAt }
        )
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
}

data class UserPreferences(
    val firstUseSeen: Boolean,
    val favorites: List<SavedTemplate>
)

object SavedTemplateCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(template: SavedTemplate): String = listOf(
        template.id,
        template.name,
        template.rawSyntax,
        template.goalId,
        template.riskLevel.name,
        template.createdAt.toString()
    ).joinToString("|") { encoder.encodeToString(it.toByteArray(StandardCharsets.UTF_8)) }

    fun decode(value: String): SavedTemplate? = runCatching {
        val fields = value.split('|').map { String(decoder.decode(it), StandardCharsets.UTF_8) }
        require(fields.size == 6)
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
