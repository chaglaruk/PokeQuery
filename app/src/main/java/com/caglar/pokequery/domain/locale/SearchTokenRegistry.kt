package com.caglar.pokequery.domain.locale

/**
 * v0.5.2 (Fix 8): dedicated search-token localization model with metadata.
 *
 * Pokémon GO search tokens are NOT translatable by generic machine translation — the game
 * client parses a fixed token grammar, and localized clients may or may not accept a
 * translated form. This model is the SINGLE SOURCE OF TRUTH for which English tokens have a
 * Turkish candidate and, critically, HOW TRUSTWORTHY that candidate is.
 *
 * We never fake verification. Every token starts as [TokenVerification.UNTESTED] or
 * [TokenVerification.BETA] and is only promoted to [TokenVerification.VERIFIED] after a
 * human confirms the token's behavior against a live localized Pokémon GO client. See
 * docs/localization/turkish_verification_matrix.md for the spot-check protocol.
 *
 * This registry is intentionally SEPARATE from [com.caglar.pokequery.domain.engine.SearchTermMapper]'s
 * active translation map: the mapper contains only tokens we are willing to emit, while this
 * registry documents the FULL set of important tokens including risky/untested ones that must
 * NOT be emitted yet. The UI (Knowledge Base) uses the registry to show verification badges
 * and language-sensitivity; the mapper consults it before trusting a Turkish token.
 */

/** How thoroughly a token's localized form has been checked. Ordered worst→best. */
enum class TokenVerification {
    /** No candidate tested at all. Do not emit a translation. */
    UNTESTED,
    /** A candidate exists but behavior in a localized client is risky/contested. */
    RISKY,
    /** A candidate is community-sourced and plausibly correct but unconfirmed. */
    BETA,
    /** A human confirmed the token works in a live localized Pokémon GO client. */
    VERIFIED
}

/**
 * Metadata for one search token.
 *
 * @property english        The canonical English token used by default / safe output.
 * @property turkish        The Turkish candidate, or null if none is known.
 * @property status         Verification status. Drives KB badges and copy policy.
 * @property languageSensitive  True if the localized client is known/expected to need a
 *                              translated form (so getting it wrong silently returns no results).
 * @property example        A concrete search-string example using this token.
 * @property commonMistake  A frequently-wrong substitution, for the KB "Common mistake" row.
 * @property notes          Free-form caveats / contesting candidates.
 */
data class SearchTokenMetadata(
    val english: String,
    val turkish: String?,
    val status: TokenVerification,
    val languageSensitive: Boolean,
    val example: String,
    val commonMistake: String? = null,
    val notes: String? = null
) {
    /** True only when this token is safe to EMIT in a Turkish search string. */
    val safeToEmit: Boolean get() = status == TokenVerification.VERIFIED
}

/**
 * The registry of important Pokémon GO search tokens with Turkish-localization metadata.
 *
 * Status reflects the current (conservative) state of verification:
 *   - Tokens already emitted by SearchTermMapper are marked BETA (community-sourced, the
 *     existing "verify before use" contract), NOT VERIFIED — because none have been
 *     confirmed against a live Turkish client in this project.
 *   - Tokens with no agreed candidate are UNTESTED.
 *   - Newer/special tokens (@special, megaevolve, fusion, dynamax, gigantamax) are UNTESTED
 *     until someone confirms them — we explicitly do NOT guess.
 */
object SearchTokenRegistry {

    val tokens: List<SearchTokenMetadata> = listOf(
        SearchTokenMetadata("shiny", "parlak", TokenVerification.BETA, true, "shiny", "shiny (English always works; localized form is unverified)"),
        SearchTokenMetadata("traded", "takaslanan", TokenVerification.BETA, true, "!traded", "takas edilmiş (KB description) vs takaslanan (map) — contesting candidates"),
        SearchTokenMetadata("count", "toplam", TokenVerification.RISKY, true, "count2-", "Contesting: 'toplam' (map) vs 'sayı' (localization plan) vs 'sayısı' (KB). Do not trust."),
        SearchTokenMetadata("favorite", "favori", TokenVerification.BETA, true, "favorite", "favourite (UK spelling) is also accepted by the game; localized form unverified"),
        SearchTokenMetadata("lucky", "şanslı", TokenVerification.BETA, true, "lucky"),
        SearchTokenMetadata("legendary", "efsanevi", TokenVerification.BETA, true, "legendary"),
        SearchTokenMetadata("mythical", "mistik", TokenVerification.RISKY, true, "mythical", "Candidate 'mistik' unconfirmed"),
        SearchTokenMetadata("shadow", "gölge", TokenVerification.BETA, true, "shadow"),
        SearchTokenMetadata("purified", "arıtılmış", TokenVerification.RISKY, true, "purified", "Candidate 'arıtılmış' unconfirmed"),
        SearchTokenMetadata("costume", "kostümlü", TokenVerification.BETA, true, "costume"),
        SearchTokenMetadata("background", "arka planlı", TokenVerification.RISKY, true, "background", "Compound candidate; exact form in-game unverified"),
        SearchTokenMetadata("specialbackground", "özel arka planlı", TokenVerification.RISKY, true, "specialbackground", "Multi-word candidate; spacing behavior in-game unverified"),
        SearchTokenMetadata("locationbackground", "konum arka planlı", TokenVerification.RISKY, true, "locationbackground", "Multi-word candidate; spacing behavior in-game unverified"),
        SearchTokenMetadata("ultrabeast", "ultra canavar", TokenVerification.RISKY, true, "ultrabeast", "Multi-word candidate; unconfirmed"),
        SearchTokenMetadata("age", "yaş", TokenVerification.BETA, true, "age365-"),
        SearchTokenMetadata("distance", "mesafe", TokenVerification.BETA, true, "distance100-"),
        SearchTokenMetadata("attack", "saldırı", TokenVerification.BETA, true, "0-1attack"),
        SearchTokenMetadata("defense", "savunma", TokenVerification.BETA, true, "3-4defense"),
        SearchTokenMetadata("hp", "can", TokenVerification.BETA, true, "3-4hp", "HP localization is known to vary; 'can' per explicit requirement but verify"),
        // --- Newer/special tokens: explicitly UNTESTED — we do not guess translations. ---
        SearchTokenMetadata("@special", null, TokenVerification.UNTESTED, true, "@special", notes = "Special move filter. No Turkish candidate known; English token likely required."),
        SearchTokenMetadata("megaevolve", null, TokenVerification.UNTESTED, true, "megaevolve", notes = "Mega evolution filter. No Turkish candidate known."),
        SearchTokenMetadata("fusion", null, TokenVerification.UNTESTED, true, "fusion", notes = "Fusion filter (Duo Primals). No Turkish candidate known."),
        SearchTokenMetadata("dynamax", null, TokenVerification.UNTESTED, true, "dynamax", notes = "Dynamax filter. Proper noun; English token likely required."),
        SearchTokenMetadata("gigantamax", null, TokenVerification.UNTESTED, true, "gigantamax", notes = "Gigantamax filter. Proper noun; English token likely required.")
    )

    /** Lookup by canonical English token. */
    fun byEnglish(token: String): SearchTokenMetadata? =
        tokens.firstOrNull { it.english.equals(token, ignoreCase = true) }

    /** Tokens that are NOT safe to emit in Turkish output. */
    fun unsafeToEmit(): List<SearchTokenMetadata> = tokens.filterNot { it.safeToEmit }

    /** Tokens explicitly marked language-sensitive (a wrong localized form returns nothing). */
    fun languageSensitive(): List<SearchTokenMetadata> = tokens.filter { it.languageSensitive }

    /** All tokens still requiring verification before they can be trusted. */
    fun unverifiedOrBeta(): List<SearchTokenMetadata> =
        tokens.filter { it.status != TokenVerification.VERIFIED }
}
