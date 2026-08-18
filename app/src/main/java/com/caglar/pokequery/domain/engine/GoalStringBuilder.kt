package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.GeneratedString

/**
 * Builds the final generated string shown on a goal-detail screen from an engine
 * base goal plus the user's optional protection toggles.
 *
 * The base goal's `rawSyntax` is already language-translated by [StringBuilderEngine].
 * Optional protections are translated through the same [SearchTermMapper] before
 * deduplication so non-English outputs do not receive duplicate exclusions.
 */
object GoalStringBuilder {

    private val passthroughGoals = setOf("hundo_check", "nundo_finder", "pvp_candidates")

    private fun syntaxTokens(raw: String): Set<String> =
        raw.split('&', ',', ';', ':')
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet()

    fun buildFinal(
        baseGoal: GeneratedString,
        optionalProtections: List<String>,
        language: String = "English"
    ): GeneratedString {
        if (baseGoal.goalId in passthroughGoals) return baseGoal

        val existing = baseGoal.rawSyntax
        val existingTokens = syntaxTokens(existing)
        val translatedProtections = optionalProtections
            .distinct()
            .map { token -> "!${SearchTermMapper.translateSyntax(token, language)}" }

        val toAdd = translatedProtections
            .filterNot { it in existingTokens }
            .joinToString("&")

        if (toAdd.isEmpty()) return baseGoal

        val merged = if (existing.isBlank()) toAdd else "$existing&$toAdd"
        return baseGoal.copy(rawSyntax = merged)
    }
}
