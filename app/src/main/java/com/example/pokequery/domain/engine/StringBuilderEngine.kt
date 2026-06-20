package com.example.pokequery.domain.engine

import com.example.pokequery.data.model.GeneratedString
import com.example.pokequery.data.model.RiskLevel

object StringBuilderEngine {

    val DEFAULT_PROTECTIONS = listOf(
        "shiny", "legendary", "mythical", "ultrabeast", "costume", "background", "locationbackground", "specialbackground",
        "shadow", "purified", "favorite", "lucky", "#", "traded", "4*"
    )
    
    val COUNT_MANDATORY_PROTECTIONS = listOf(
        "shiny", "lucky", "legendary", "mythical", "shadow", "purified", "favorite", "traded"
    )

    fun buildString(
        baseQuery: String,
        protections: List<String> = DEFAULT_PROTECTIONS,
        explanation: String,
        riskLevel: RiskLevel = RiskLevel.Low
    ): GeneratedString {
        
        var query = baseQuery
        val generatedWarnings = mutableListOf<String>()
        
        // Safety check: Never generate |
        if (query.contains("|")) {
            query = query.replace("|", ",")
            generatedWarnings.add("The '|' operator is unsupported and was replaced with ','.")
        }

        val exclusions = mutableListOf<String>()
        val highRiskIncluded = mutableListOf<String>()

        val protectionsToAdd = protections.filter { !baseQuery.contains(it) }
        if (protectionsToAdd.isNotEmpty()) {
            val protectionStr = protectionsToAdd.joinToString("&") { "!$it" }
            query = if (query.isEmpty()) protectionStr else "$query&$protectionStr"
        }
        exclusions.addAll(protectionsToAdd)

        // Check what high risk things we are exposing based on standard DEFAULT_PROTECTIONS
        DEFAULT_PROTECTIONS.forEach {
            if (baseQuery.contains(it) || !protections.contains(it)) {
                highRiskIncluded.add(it)
            }
        }

        // Mandatory count[N] safety check
        if (query.contains("count")) {
            val missingExclusions = COUNT_MANDATORY_PROTECTIONS.filter { !query.contains("!$it") }
            if (missingExclusions.isNotEmpty()) {
                val protectionStr = missingExclusions.joinToString("&") { "!$it" }
                query = "$query&$protectionStr"
                exclusions.addAll(missingExclusions.filter { !exclusions.contains(it) })
            }
            generatedWarnings.add("Count output: Count is based on Pokédex species number and may not distinguish shiny/form/costume differences.")
        }
        
        // Trade warning check
        if (query.contains("trade")) {
            generatedWarnings.add("Trade disclaimer: Real trade eligibility depends on friendship level and cannot be guaranteed by search strings.")
        }

        return GeneratedString(
            rawSyntax = query,
            plainLanguageExplanation = explanation,
            excludedCategories = exclusions,
            includedHighRiskCategories = highRiskIncluded,
            riskLevel = if (query.contains("count") && riskLevel == RiskLevel.Low) RiskLevel.Medium else riskLevel,
            warnings = generatedWarnings
        )
    }
}
