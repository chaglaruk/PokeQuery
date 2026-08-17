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

    private data class KnownTokenInfo(val category: String, val description: String)

    private val knownTokens = mapOf(
        "shiny" to KnownTokenInfo("shiny", "Filters for Shiny Pokémon"),
        "legendary" to KnownTokenInfo("legendary", "Filters for Legendary Pokémon"),
        "mythical" to KnownTokenInfo("mythical", "Filters for Mythical Pokémon (risky — cannot be re-obtained easily)"),
        "shadow" to KnownTokenInfo("shadow", "Filters for Shadow Pokémon"),
        "purified" to KnownTokenInfo("purified", "Filters for Purified Pokémon"),
        "lucky" to KnownTokenInfo("lucky", "Filters for Lucky Pokémon"),
        "favorite" to KnownTokenInfo("favorite", "Filters for Favorite (starred) Pokémon"),
        "costume" to KnownTokenInfo("costume", "Filters for Costume Pokémon"),
        "traded" to KnownTokenInfo("traded", "Filters for Pokémon that have been traded"),
        "ultrabeast" to KnownTokenInfo("ultrabeast", "Filters for Ultra Beasts"),
        "background" to KnownTokenInfo("background", "Filters for Special Background Pokémon"),
        "locationbackground" to KnownTokenInfo("locationbackground", "Filters for Location Card Background Pokémon"),
        "specialbackground" to KnownTokenInfo("specialbackground", "Filters for Event Background Pokémon"),
        "defender" to KnownTokenInfo("defender", "Filters for Pokémon currently defending a Gym"),
        "4*" to KnownTokenInfo("iv_band", "Perfect IV (100% appraisal)"),
        "3*" to KnownTokenInfo("iv_band", "High IV (80-99% appraisal)"),
        "2*" to KnownTokenInfo("iv_band", "Mid IV (50-79% appraisal)"),
        "1*" to KnownTokenInfo("iv_band", "Low IV (0-50% appraisal) — cleanup candidate"),
        "0*" to KnownTokenInfo("iv_band", "Lowest IV band — may include 0% IV finds"),
        "age0" to KnownTokenInfo("age_filter", "Caught today"),
        "age1" to KnownTokenInfo("age_filter", "Caught yesterday or today"),
        "age365-" to KnownTokenInfo("age_filter", "Caught at least 365 days ago"),
        "distance100-" to KnownTokenInfo("distance_filter", "Traded from 100+ km away"),
        "distance1000-" to KnownTokenInfo("distance_filter", "Traded from 1000+ km away"),
        "hp" to KnownTokenInfo("iv_stat", "HP IV filter"),
        "attack" to KnownTokenInfo("iv_stat", "Attack IV filter"),
        "defense" to KnownTokenInfo("iv_stat", "Defense IV filter"),
        "cp" to KnownTokenInfo("cp_range", "CP range filter"),
        "#" to KnownTokenInfo("tag", "Tag filter")
    )

    private val riskyTokens = setOf("shiny", "legendary", "mythical", "lucky")

    private fun structuredCategory(clean: String): String? = when {
        clean.matches(Regex("\\d+\\*")) -> "iv_band"
        clean.matches(Regex("\\d+(?:-\\d+)?(?:hp|attack|defense)")) -> "iv_stat"
        clean.matches(Regex("(?:hp|attack|defense)[<>]?\\d*(?:-\\d*)?")) -> "iv_stat"
        clean.matches(Regex("cp-?\\d+(?:-\\d*)?")) -> "cp_range"
        clean.matches(Regex("age\\d+(?:-\\d*)?")) -> "age_filter"
        clean.matches(Regex("distance\\d+(?:-\\d*)?")) -> "distance_filter"
        clean.matches(Regex("count\\d*(?:-\\d*)?")) -> "count_filter"
        clean.matches(Regex("@[^&,:;]+")) -> "special_move"
        else -> null
    }

    private fun descriptionFor(category: String): String = when (category) {
        "iv_band" -> "IV appraisal band filter"
        "iv_stat" -> "Individual IV stat filter"
        "cp_range" -> "CP range filter"
        "age_filter" -> "Age (days since caught) filter"
        "distance_filter" -> "Trade distance filter"
        "count_filter" -> "Species count filter"
        "special_move" -> "Special move / form filter"
        else -> "Unknown token — verify this works in Pokémon GO"
    }

    private fun computePrecision(tokens: List<ExplainedToken>): SearchPrecision {
        val categories = tokens.map { it.category }.toSet()
        val cleanTokens = tokens.filter { !it.isExclusion }.map { it.token.removePrefix("!") }
        return when {
            cleanTokens.any { it in exactTokens } -> SearchPrecision.EXACT
            cleanTokens.any { it in shortlistTokens } -> SearchPrecision.SHORTLIST
            categories.any { it in setOf("iv_band", "iv_stat", "age_filter", "distance_filter", "cp_range", "count_filter") } -> SearchPrecision.APPROXIMATE
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

    fun explain(input: String): ExplainedString {
        val raw = input.trim()
        if (raw.isBlank()) return ExplainedString("", emptyList(), RiskLevel.Info, false, "Empty search string")

        val parts = raw.split("&").filter { it.isNotBlank() }
        val tokens = parts.map { part ->
            val isExclusion = part.startsWith("!")
            val clean = if (isExclusion) part.removePrefix("!") else part
            val known = knownTokens[clean]
            val category = known?.category ?: structuredCategory(clean) ?: "unknown"
            val description = known?.description ?: descriptionFor(category)
            val risk = if (clean in riskyTokens) RiskLevel.Medium else if (category == "unknown") RiskLevel.Low else RiskLevel.Info

            ExplainedToken(
                token = part,
                category = category,
                isExclusion = isExclusion,
                description = description,
                riskHint = risk
            )
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
            if (inclusions.isNotEmpty()) append(" looks for ${inclusions.joinToString(", ") { it.token }}")
            if (exclusions.isNotEmpty()) append(" and excludes ${exclusions.joinToString(", ") { it.token }}")
            if (inclusions.isEmpty() && exclusions.isEmpty()) append(" has no recognized tokens")
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
