package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.GeneratedString

/**
 * Builds the final generated string shown on a goal-detail screen from an engine
 * base goal plus the user's optional protection toggles.
 *
 * v0.4.2 safety patch (Fix 1, audit BUG-001):
 *
 * The previous screen-layer implementation re-derived the base query by splitting
 * `baseGoal.rawSyntax` on '&' and keeping only the first non-exclusion token, e.g.
 * turning `count2-&!traded` into `count2-` and silently dropping the engine-mandated
 * `!traded` term. This helper replaces that logic so that:
 *   - engine-mandated terms (e.g. `!traded` on trade_fodder / lucky_trade) are never
 *     removed;
 *   - optional protections are strictly additive and deduplicated against whatever is
 *     already present (so `!traded&!traded` can never be produced);
 *   - pass-through goals (hundo_check, nundo_finder, pvp_candidates) are returned
 *     unchanged, since they intentionally carry no cleanup protections.
 *
 * The base goal's `rawSyntax` is already language-translated by `StringBuilderEngine`;
 * optional protections are translated here through the same `SearchTermMapper` so the
 * appended terms match the selected language.
 */
object GoalStringBuilder {

    private val passthroughGoals = setOf("hundo_check", "nundo_finder", "pvp_candidates")

    fun buildFinal(
        baseGoal: GeneratedString,
        optionalProtections: List<String>,
        language: String = "English"
    ): GeneratedString {
        // Pass-through inspection goals: no protections, no re-wrap.
        if (baseGoal.goalId in passthroughGoals) return baseGoal

        val existing = baseGoal.rawSyntax
        val alreadyPresent = optionalProtections.filter { existing.contains("!$it") }
        val toAdd = optionalProtections
            .filterNot { existing.contains("!$it") }
            .joinToString("&") { token -> "!${SearchTermMapper.translateSyntax(token, language)}" }

        if (alreadyPresent.isEmpty() && toAdd.isEmpty()) return baseGoal

        val merged = if (toAdd.isEmpty()) existing else "$existing&$toAdd"
        return baseGoal.copy(rawSyntax = merged)
    }
}
