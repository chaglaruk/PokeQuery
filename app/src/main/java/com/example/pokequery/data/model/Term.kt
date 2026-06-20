package com.example.pokequery.data.model

data class Term(
    val id: String,
    val syntax: String,
    val category: String,
    val tier: String,
    val descriptionTr: String,
    val descriptionEn: String,
    val riskLevel: RiskLevel,
    val sourceUrl: String,
    val lastVerified: String,
    val knownQuirks: String?
)

enum class RiskLevel { Low, Medium, High }
