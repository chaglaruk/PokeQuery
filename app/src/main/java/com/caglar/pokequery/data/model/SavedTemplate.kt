package com.caglar.pokequery.data.model

import java.util.UUID

data class SavedTemplate(
    val id: String,
    val name: String,
    val rawSyntax: String,
    val goalId: String,
    val riskLevel: RiskLevel,
    val createdAt: Long
) {
    fun asGeneratedString() = GeneratedString(
        rawSyntax = rawSyntax,
        plainLanguageExplanation = "Saved search template. Review all matches in the game before acting.",
        protectedCategories = emptyList(),
        includedHighRiskCategories = emptyList(),
        riskLevel = riskLevel,
        goalId = goalId,
        title = name
    )

    companion object {
        fun from(generated: GeneratedString) = SavedTemplate(
            id = UUID.randomUUID().toString(),
            name = generated.title,
            rawSyntax = generated.rawSyntax,
            goalId = generated.goalId,
            riskLevel = generated.riskLevel,
            createdAt = System.currentTimeMillis()
        )
    }
}
