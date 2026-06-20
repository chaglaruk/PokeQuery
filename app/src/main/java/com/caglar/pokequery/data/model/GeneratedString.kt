package com.caglar.pokequery.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedString(
    val rawSyntax: String,
    val plainLanguageExplanation: String,
    val protectedCategories: List<String>,
    val includedHighRiskCategories: List<String>,
    val riskLevel: RiskLevel,
    val warnings: List<String> = emptyList(),
    val goalId: String = "custom",
    val title: String = "Custom Search",
    val scopeBreadth: String = "Moderate"
)
