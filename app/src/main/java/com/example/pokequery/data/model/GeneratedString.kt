package com.example.pokequery.data.model

data class GeneratedString(
    val rawSyntax: String,
    val plainLanguageExplanation: String,
    val excludedCategories: List<String>,
    val includedHighRiskCategories: List<String>,
    val riskLevel: RiskLevel,
    val warnings: List<String> = emptyList()
)
