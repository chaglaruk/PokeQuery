package com.example.pokequery.domain.engine

import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel

object StringBuilderEngine {

    val DEFAULT_PROTECTIONS = listOf(
        "shiny", "legendary", "mythical", "ultrabeast", "costume", "background", "locationbackground", "specialbackground",
        "shadow", "purified", "favorite", "lucky", "#", "traded", "4*"
    )
    
    val COUNT_MANDATORY_PROTECTIONS = listOf(
        "shiny", "lucky", "legendary", "mythical", "shadow", "purified", "favorite", "traded", "costume"
    )

    fun buildString(
        baseQuery: String,
        protections: List<String> = DEFAULT_PROTECTIONS,
        explanation: String,
        riskLevel: RiskLevel = RiskLevel.Low,
        goalId: String = "custom",
        title: String = "Custom Search"
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

        val protectedCategories = (DEFAULT_PROTECTIONS + COUNT_MANDATORY_PROTECTIONS)
            .distinct()
            .filter { query.contains("!$it") }

        return GeneratedString(
            rawSyntax = query,
            plainLanguageExplanation = explanation,
            protectedCategories = protectedCategories,
            includedHighRiskCategories = DEFAULT_PROTECTIONS.filterNot { query.contains("!$it") },
            riskLevel = if (query.contains("count") && riskLevel == RiskLevel.Low) RiskLevel.Medium else riskLevel,
            warnings = generatedWarnings,
            goalId = goalId,
            title = title
        )
    }

    fun buildGoal(goalId: String, include0Star: Boolean = false, customQuery: String = ""): GeneratedString {
        val (query, explanation, risk, title) = when (goalId) {
            "safe_cleanup" -> GoalSpec(
                if (include0Star) "0*,1*" else "1*",
                "This is a REVIEW string targeting low-value candidates. It is not an automatic transfer command.",
                if (include0Star) RiskLevel.Medium else RiskLevel.Low,
                "Safe Cleanup"
            )
            "candy_prep" -> GoalSpec(
                "count2-",
                "Finds extras. Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.",
                RiskLevel.Medium,
                "2x Candy Prep"
            )
            "trade_fodder" -> GoalSpec(
                "count2-&!traded",
                "Finds candidates for trading. Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.",
                RiskLevel.Medium,
                "Trade Fodder"
            )
            "hundo_check" -> GoalSpec(
                "4*",
                "Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15.",
                RiskLevel.Info,
                "Hundo Check"
            )
            "untagged" -> GoalSpec("!#", "Finds Pokémon without any tags.", RiskLevel.Low, "Untagged Cleanup")
            "expert" -> GoalSpec(customQuery, "Custom search string. Review all matches in the game before acting.", RiskLevel.Medium, "Custom Search")
            else -> GoalSpec(customQuery, "Custom search string.", RiskLevel.Medium, "Custom Search")
        }
        return buildString(
            baseQuery = query,
            protections = if (goalId == "hundo_check") emptyList() else DEFAULT_PROTECTIONS,
            explanation = explanation,
            riskLevel = risk,
            goalId = goalId,
            title = title
        )
    }

    private data class GoalSpec(
        val query: String,
        val explanation: String,
        val risk: RiskLevel,
        val title: String
    )
}
