package com.caglar.pokequery.data.model

import kotlinx.serialization.Serializable

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

@Serializable
enum class RiskLevel { Info, Low, Medium, High }
