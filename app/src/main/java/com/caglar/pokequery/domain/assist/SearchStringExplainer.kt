package com.caglar.pokequery.domain.assist

import com.caglar.pokequery.data.model.RiskLevel

data class ExplainedToken(
    val token: String,
    val category: String,
    val isExclusion: Boolean,
    val description: String,
    val riskHint: RiskLevel = RiskLevel.Info
)

enum class SearchPrecision { EXACT, SHORTLIST, APPROXIMATE, NEEDS_VERIFICATION, UNKNOWN }

data class ExplainedString(
    val original: String,
    val tokens: List<ExplainedToken>,
    val totalRisk: RiskLevel,
    val hasUnknownTokens: Boolean,
    val summary: String,
    val precision: SearchPrecision = SearchPrecision.NEEDS_VERIFICATION,
    val precisionLabel: String = "Needs verification",
    val scopeBreadth: String = "Moderate"
)

object SearchStringExplainer {
    private val exactTokens = setOf("4*", "0attack", "0defense", "0hp", "nundo")
    private val shortlistTokens = setOf("shiny", "legendary", "shadow", "purified", "lucky", "favorite", "costume", "traded")
    private val approximateTokens = setOf("3*", "2*", "1*", "0*", "age", "distance", "count", "cp")

    private fun computePrecision(tokens: List<ExplainedToken>): SearchPrecision {
        val categories = tokens.map { it.category }.toSet()
        val cleanTokens = tokens.filter { !it.isExclusion }.map { it.token }
        return when {
            cleanTokens.any { it in exactTokens } -> SearchPrecision.EXACT
            cleanTokens.any { it in shortlistTokens } -> SearchPrecision.SHORTLIST
            categories.any { it in setOf("iv_band", "age_filter", "distance_filter", "cp_range", "count_filter") } -> SearchPrecision.APPROXIMATE
            tokens.any { it.category == "unknown" } -> SearchPrecision.NEEDS_VERIFICATION
            else -> SearchPrecision.NEEDS_VERIFICATION
        }
    }

    private fun computeScopeBreadth(tokens: List<ExplainedToken>): String {
        val cleanCount = tokens.count { !it.isExclusion }
        return when {
            cleanCount == 0 -> "All (no filter)"
            cleanCount <= 1 -> "Very Narrow"
            cleanCount <= 2 -> "Narrow"
            cleanCount <= 3 -> "Moderate"
            else -> "Broad"
        }
    }

    private val knownTokens = mapOf(
        "shiny" to "Filters for Shiny Pokémon",
        "legendary" to "Filters for Legendary Pokémon",
        "mythical" to "Filters for Mythical Pokémon (risky — cannot be re-obtained easily)",
        "shadow" to "Filters for Shadow Pokémon",
        "purified" to "Filters for Purified Pokémon",
        "lucky" to "Filters for Lucky Pokémon",
        "favorite" to "Filters for Favorite (starred) Pokémon",
        "costume" to "Filters for Costume Pokémon",
        "traded" to "Filters for Pokémon that have been traded",
        "ultrabeast" to "Filters for Ultra Beasts",
        "background" to "Filters for Special Background Pokémon",
        "locationbackground" to "Filters for Location Card Background Pokémon",
        "specialbackground" to "Filters for Event Background Pokémon",
        "defender" to "Filters for Pokémon currently defending a Gym",
        "4*" to "Perfect IV (100% appraisal)",
        "3*" to "High IV (80-99% appraisal)",
        "2*" to "Mid IV (50-79% appraisal)",
        "1*" to "Low IV (0-50% appraisal) — cleanup candidate",
        "0*" to "Lowest IV band — may include 0% IV finds",
        "age0" to "Caught today",
        "age1" to "Caught yesterday or today",
        "age365-" to "Caught at least 365 days ago",
        "distance100-" to "Traded from 100+ km away",
        "distance1000-" to "Traded from 1000+ km away",
        "hp" to "HP IV filter",
        "attack" to "Attack IV filter",
        "defense" to "Defense IV filter",
        "cp" to "CP range filter",
        "!" to "NOT / exclusion operator — must not have this tag",
        "&" to "AND operator — all conditions must match",
        "#" to "Tag filter"
    )

    private val riskyTokens = setOf("shiny", "legendary", "mythical", "lucky")

    fun explain(input: String): ExplainedString {
        val raw = input.trim()
        if (raw.isBlank()) return ExplainedString("", emptyList(), RiskLevel.Info, false, "Empty search string")

        val parts = raw.split("&").filter { it.isNotBlank() }
        val tokens = parts.mapNotNull { part ->
            val isExclusion = part.startsWith("!")
            val clean = if (isExclusion) part.removePrefix("!") else part
            val known = knownTokens.entries.firstOrNull { (key, _) ->
                clean == key || clean.startsWith(key)
            }
            if (known != null) {
                val risk = if (known.key in riskyTokens) RiskLevel.Medium else RiskLevel.Info
                ExplainedToken(
                    token = part,
                    category = known.key,
                    isExclusion = isExclusion,
                    description = known.value,
                    riskHint = risk
                )
            } else {
                val category = when {
                    clean.matches(Regex("\\d+\\*")) -> "iv_band"
                    clean.matches(Regex("(hp|attack|defense)[<>]?\\d*")) -> "iv_stat"
                    clean.matches(Regex("cp-?\\d*")) -> "cp_range"
                    clean.matches(Regex("age\\d+")) -> "age_filter"
                    clean.matches(Regex("distance\\d+")) -> "distance_filter"
                    clean.matches(Regex("count\\d*")) -> "count_filter"
                    clean.matches(Regex("@\\w*")) -> "special_move"
                    else -> "unknown"
                }
                ExplainedToken(
                    token = part,
                    category = category,
                    isExclusion = isExclusion,
                    description = when (category) {
                        "iv_band" -> "IV appraisal band filter"
                        "iv_stat" -> "Individual IV stat filter"
                        "cp_range" -> "CP range filter"
                        "age_filter" -> "Age (days since caught) filter"
                        "distance_filter" -> "Trade distance filter"
                        "count_filter" -> "Species count filter"
                        "special_move" -> "Special move / form filter"
                        else -> "Unknown token — verify this works in Pokémon GO"
                    },
                    riskHint = if (category == "unknown") RiskLevel.Low else RiskLevel.Info
                )
            }
        }

        val hasUnknown = tokens.any { it.category == "unknown" }
        val risks = tokens.map { it.riskHint }
        val totalRisk = when {
            risks.contains(RiskLevel.Medium) -> RiskLevel.Medium
            risks.contains(RiskLevel.Low) -> RiskLevel.Low
            else -> RiskLevel.Info
        }
        val inclusions = tokens.filter { !it.isExclusion }
        val exclusions = tokens.filter { it.isExclusion }
        val summary = buildString {
            append("This search string")
            if (inclusions.isNotEmpty()) {
                append(" looks for ${inclusions.joinToString(", ") { it.token }}")
            }
            if (exclusions.isNotEmpty()) {
                append(" and excludes ${exclusions.joinToString(", ") { it.token }}")
            }
            if (inclusions.isEmpty() && exclusions.isEmpty()) {
                append(" has no recognized tokens")
            }
        }

        val precision = computePrecision(tokens)
        val precisionLabel = when (precision) {
            SearchPrecision.EXACT -> "Exact search"
            SearchPrecision.SHORTLIST -> "Shortlist"
            SearchPrecision.APPROXIMATE -> "Approximate"
            SearchPrecision.NEEDS_VERIFICATION -> "Needs verification"
            SearchPrecision.UNKNOWN -> "Unknown"
        }
        val scope = computeScopeBreadth(tokens)

        return ExplainedString(raw, tokens, totalRisk, hasUnknown, summary, precision, precisionLabel, scope)
    }
}
