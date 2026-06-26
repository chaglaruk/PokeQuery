package com.caglar.pokequery.data.model

import kotlinx.serialization.Serializable

/**
 * Knowledge-base term verification status (Package 8).
 *
 * Do not fake verification. If a token's behavior in a localized Pokémon GO client is
 * uncertain, mark it BETA or NEEDS_VERIFICATION — never VERIFIED.
 */
@Serializable
enum class VerificationStatus {
    VERIFIED,
    BETA,
    NEEDS_VERIFICATION
}

data class Term(
    val id: String,
    val title: String? = null,
    val syntax: String,
    val category: String,
    val tier: String,
    val descriptionTr: String,
    val descriptionEn: String,
    val riskLevel: RiskLevel,
    val sourceUrl: String,
    val lastVerified: String,
    val knownQuirks: String?,
    // Package 8 optional metadata (null/needs_verification when absent).
    val verificationStatus: VerificationStatus = VerificationStatus.NEEDS_VERIFICATION,
    val safetyLevel: String? = null,
    val languageSensitive: Boolean? = null,
    val example: String? = null,
    val commonMistake: String? = null
)

@Serializable
enum class RiskLevel { Info, Low, Medium, High }
