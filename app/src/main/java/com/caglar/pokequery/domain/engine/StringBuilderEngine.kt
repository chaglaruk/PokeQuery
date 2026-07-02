package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.GeneratedString
import com.caglar.pokequery.data.model.RiskLevel

object StringBuilderEngine {

    val DEFAULT_PROTECTIONS = listOf(
        "shiny", "legendary", "mythical", "ultrabeast", "costume", "background", "locationbackground", "specialbackground",
        "shadow", "purified", "favorite", "lucky", "#", "traded", "4*"
    )

    val COUNT_MANDATORY_PROTECTIONS = listOf(
        "shiny", "lucky", "legendary", "mythical", "shadow", "purified", "favorite", "traded", "costume",
        // v0.5.5 (Fix 6): count/duplicate-cleanup queries must also protect the valuable
        // variant categories — Ultra Beasts and the background variants. A bare `count2-` finds
        // duplicates by Pokédex species number and does NOT distinguish these forms, so without
        // these exclusions a valuable Ultra Beast or special-background Pokémon could land in a
        // transfer/cleanup list. These now join shiny/legendary/etc. as mandatory.
        "ultrabeast", "background", "locationbackground", "specialbackground"
    )

    fun buildString(
        baseQuery: String,
        protections: List<String> = DEFAULT_PROTECTIONS,
        explanation: String,
        riskLevel: RiskLevel = RiskLevel.Low,
        goalId: String = "custom",
        title: String = "Custom Search",
        language: String = "English"
    ): GeneratedString {

        var query = baseQuery
        val generatedWarnings = mutableListOf<String>()

        // Safety check: Never generate |
        if (query.contains("|")) {
            query = query.replace("|", ",")
            generatedWarnings.add("The '|' operator is unsupported and was replaced with ','.")
        }

        val protectionsToAdd = protections.filter { !baseQuery.contains("!$it") }
        if (protectionsToAdd.isNotEmpty()) {
            val protectionStr = protectionsToAdd.joinToString("&") { "!$it" }
            query = if (query.isEmpty()) protectionStr else "$query&$protectionStr"
        }

        // Mandatory count[N] safety check
        if (query.contains("count")) {
            val missingExclusions = COUNT_MANDATORY_PROTECTIONS.filter { !query.contains("!$it") }
            if (missingExclusions.isNotEmpty()) {
                val protectionStr = missingExclusions.joinToString("&") { "!$it" }
                query = "$query&$protectionStr"
            }
            generatedWarnings.add("Count output: Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.")
        }

        // Trade warning check
        if (goalId == "trade_fodder") {
            generatedWarnings.add("Trade disclaimer: Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.")
        }

        val unverified = SearchTermMapper.findUnverifiedTokens(query, language)
        if (unverified.isNotEmpty()) {
            generatedWarnings.add("Some search terms are unverified and will fall back to English.")
        }

        val protectedCategories = (DEFAULT_PROTECTIONS + COUNT_MANDATORY_PROTECTIONS)
            .distinct()
            .filter { query.contains("!$it") }

        val rawSyntax = SearchTermMapper.translateSyntax(query, language)

        return GeneratedString(
            rawSyntax = rawSyntax,
            plainLanguageExplanation = explanation,
            protectedCategories = protectedCategories,
            includedHighRiskCategories = DEFAULT_PROTECTIONS.filterNot { query.contains("!$it") },
            riskLevel = if (query.contains("count") && riskLevel == RiskLevel.Low) RiskLevel.Medium else riskLevel,
            warnings = generatedWarnings,
            goalId = goalId,
            title = title,
            scopeBreadth = calculateScopeBreadth(query)
        )
    }

    private fun calculateScopeBreadth(query: String): String {
        val lower = query.lowercase()
        if (lower == "4*" || lower == "0attack&0defense&0hp") return "Very Narrow"
        if (lower.contains("0-1attack") || lower.contains("3-4defense") || lower.contains("cp-1500") || lower.contains("cp-2500")) return "Narrow"
        if (lower.contains("count2-") && lower.contains("!traded")) return "Broad"
        if (lower.contains("count2-")) return "Broad"
        if (lower.contains("age365-") || lower.contains("distance100-")) return "Moderate"
        if (lower.contains("!shiny") && lower.contains("!legendary")) return "Moderate"
        if (lower.isEmpty() || (lower.split("&").size == 1 && !lower.contains("!"))) return "Very Broad"
        return "Moderate"
    }

    /**
     * v0.5.5 (Fix 5) — risk-model intent (inspection-only vs action-adjacent).
     *
     * The risk level assigned per goal below is intentional, not arbitrary:
     *
     *   **Inspection-only goals → Info / Low.** `hundo_check`, `nundo_finder` and
     *   `pvp_candidates` only surface Pokémon to *look at* — they do not, by themselves,
     *   suggest a cleanup/trade action, so they carry no mandatory protections and the lowest
     *   risk band. Finding a hundo to admire, a nundo to track, or a PvP IV candidate to
     *   research does not put a valuable Pokémon at risk of transfer, so a confirmation gate
     *   would add friction without adding safety.
     *
     *   **Action-adjacent / cleanup / trade goals → Medium.** `safe_cleanup`, `candy_prep`,
     *   `trade_fodder` and `lucky_trade` produce strings whose natural next step IS a transfer,
     *   evolution-for-candy, or trade — irreversible actions where a mistaken match could lose
     *   a valuable Pokémon. These route through RiskWarning, carry mandatory protections (count
     *   cleanup protects shiny/legendary/…/variants; trade goals keep the `!traded` invariant),
     *   and get the per-goal RiskMessageBuilder caution.
     *
     * This is why `4*` is Info while `count2-` is Medium even though both "find Pokémon": the
     * risk is about what the string invites the user to DO next, not what it matches. Do not
     * "normalize" these levels to Medium-for-everything or Info-for-everything without a clear
     * reason — that would either drown inspection goals in false warnings or silently remove
     * the gate from action-adjacent workflows.
     */
    fun buildGoal(goalId: String, config: String = "", customQuery: String = "", language: String = "English"): GeneratedString {
        val (query, explanation, risk, title, customProtections) = when (goalId) {
            "safe_cleanup" -> GoalSpec(
                if (config == "include0Star") "0*,1*" else "1*",
                "This is a REVIEW string targeting low-value candidates. It is not an automatic transfer command.",
                // v0.5.1 (Fix 1): Safe Cleanup is a transfer-adjacent review. Both modes route
                // through RiskWarning so the user explicitly accepts the review reminder.
                RiskLevel.Medium,
                "Safe Cleanup",
                DEFAULT_PROTECTIONS
            )
            "candy_prep" -> GoalSpec(
                "count2-",
                "Finds extras. Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.",
                RiskLevel.Medium,
                "2x Candy Prep",
                DEFAULT_PROTECTIONS
            )
            "trade_fodder" -> GoalSpec(
                "count2-&!traded",
                "Finds candidates for trading. Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.",
                RiskLevel.Medium,
                "Trade Fodder",
                DEFAULT_PROTECTIONS
            )
            "hundo_check" -> GoalSpec(
                "4*",
                "Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15.",
                RiskLevel.Info,
                "Hundo Check",
                emptyList()
            )
            "untagged" -> GoalSpec("!#", "Finds Pokémon without any tags.", RiskLevel.Low, "Untagged Cleanup", DEFAULT_PROTECTIONS)
            "nundo_finder" -> GoalSpec(
                "0attack&0defense&0hp",
                "Finds exact 0/0/0 IV Pokémon (Nundos).",
                RiskLevel.Info,
                "Nundo Finder",
                emptyList()
            )
            "pvp_candidates" -> {
                val pvpQuery = if (config == "ultra") "0-1attack&3-4defense&3-4hp&cp-2500" else "0-1attack&3-4defense&3-4hp&cp-1500"
                GoalSpec(
                    pvpQuery,
                    "Candidate search only. Final PvP rank depends on species, level, and IV spread.",
                    RiskLevel.Info,
                    "PvP IV Candidates",
                    emptyList()
                )
            }
            "lucky_trade" -> {
                val tradeQuery = if (config == "distance") "distance100-&!traded" else "age365-&!traded"
                GoalSpec(
                    tradeQuery,
                    "Finds older or distance-relevant Pokémon to review for trades. Review manually. Valuable Pokémon may appear. Trade eligibility and Lucky chance are not guaranteed by search strings.",
                    RiskLevel.Medium,
                    "Lucky Trade Prep",
                    emptyList() // We do not exclude shiny/legendary by default
                )
            }
            "expert" -> GoalSpec(customQuery, "Custom search string. Review all matches in the game before acting.", RiskLevel.Medium, "Custom Search", DEFAULT_PROTECTIONS)
            else -> GoalSpec(customQuery, "Custom search string.", RiskLevel.Medium, "Custom Search", DEFAULT_PROTECTIONS)
        }
        return buildString(
            baseQuery = query,
            protections = customProtections,
            explanation = explanation,
            riskLevel = risk,
            goalId = goalId,
            title = title,
            language = language
        )
    }

    private data class GoalSpec(
        val query: String,
        val explanation: String,
        val risk: RiskLevel,
        val title: String,
        val protections: List<String>
    )
}
