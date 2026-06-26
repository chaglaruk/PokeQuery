package com.caglar.pokequery.data.model

import java.util.UUID

/**
 * v0.6.1 — Personal Presets.
 *
 * A user-created preset derived from a Favorite, a History entry, or a freshly generated
 * goal string. Personal presets are LOCAL ONLY: never synced, never uploaded, never shared.
 * They appear in the Presets screen under a dedicated "My Presets" section, separate from
 * the built-in preset source.
 *
 * Risk gating is preserved: a Medium/High personal preset still routes through RiskWarning
 * before copy. Saving a string as a personal preset never downgrades its risk level.
 */
data class PersonalPreset(
    val id: String,
    val title: String,
    val queryString: String,
    val sourceGoalId: String?,
    val sourceFavoriteId: String?,
    val sourceHistoryId: String?,
    val riskLevel: RiskLevel,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isUserCreated: Boolean get() = true

    fun asGeneratedString(): GeneratedString = GeneratedString(
        rawSyntax = queryString,
        plainLanguageExplanation = notes
            ?: "Personal preset. Review all matches in Pokémon GO before acting.",
        protectedCategories = emptyList(),
        includedHighRiskCategories = emptyList(),
        riskLevel = riskLevel,
        goalId = sourceGoalId ?: "personal_preset",
        title = title
    )

    companion object {
        fun fromFavorite(template: SavedTemplate): PersonalPreset {
            val now = System.currentTimeMillis()
            return PersonalPreset(
                id = UUID.randomUUID().toString(),
                title = template.name,
                queryString = template.rawSyntax,
                sourceGoalId = template.goalId.takeIf { it != "legacy" && it != "preset" },
                sourceFavoriteId = template.id,
                sourceHistoryId = null,
                riskLevel = template.riskLevel,
                createdAt = now,
                updatedAt = now
            )
        }

        fun fromGenerated(generated: GeneratedString): PersonalPreset {
            val now = System.currentTimeMillis()
            return PersonalPreset(
                id = UUID.randomUUID().toString(),
                title = generated.title,
                queryString = generated.rawSyntax,
                sourceGoalId = generated.goalId,
                sourceFavoriteId = null,
                sourceHistoryId = null,
                riskLevel = generated.riskLevel,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
