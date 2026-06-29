package com.caglar.pokequery.domain.locale

/**
 * TODO(v0.7): remaining Turkish localization work
 *   1. Verify `count` candidates (toplam/sayı/sayısı) against a live Turkish client
 *   2. Verify compound protection tokens (background, locationbackground, specialbackground, ultrabeast)
 *   3. Live-verify all BETA tokens
 *   4. Resolve RISKY tokens: mistik (mythical), arıtılmış (purified)
 * See: docs/localization/turkish_verification_matrix.md
 *
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
 *
 * v0.5.5 (Fix 4): the `count` token truth is centralized here via [COUNT_CANDIDATES] +
 * [countMeta]. `count` is parser-sensitive numeric syntax (`countN-`) AND its Turkish form is
 * contested across sources, so it is NOT emitted in Turkish output (English fallback). The
 * candidates remain documented as hypotheses to test. This is the single source of truth the
 * mapper, KB and docs all agree on — there is no longer a divergence between "toplam" (old
 * map), "sayı" (localization plan) and "sayısı" (KB).
 *
 * v0.5.5 safety hotfix: the compound PARSER-SENSITIVE PROTECTION tokens (`background`,
 * `locationbackground`, `specialbackground`, `ultrabeast`) follow the same English-fallback
 * policy as `count`. Their multi-word Turkish candidates are unverified and a broken protection
 * token is dangerous (it must work to exclude a valuable Pokémon from cleanup/trade lists), so
 * they are NOT emitted in Turkish output. Candidates are captured in [compoundCandidates] as
 * hypotheses to verify live, and shown in the KB; the generated query keeps the English token.
 */
object SearchTokenRegistry {

    /**
     * v0.5.5 (Fix 4): the contesting Turkish candidates for `count` gathered in ONE place.
     * Sources:
     *   - "toplam"  : the value the legacy SearchTermMapper map used (v0.4.x–v0.5.4).
     *   - "sayı"    : docs/research/turkish_localization_plan.md spot-check matrix.
     *   - "sayısı"  : Knowledge Base description_tr for the count term.
     * None are verified against a live Turkish client. They are hypotheses for the verification
     * matrix, NOT emitted tokens. The mapper uses the English `count` fallback until one is
     * confirmed live and promoted to [TokenVerification.VERIFIED].
     */
    val COUNT_CANDIDATES: List<String> = listOf("toplam", "sayı", "sayısı")

    /**
     * v0.5.5 safety hotfix: the contesting multi-word Turkish candidates for the compound
     * PARSER-SENSITIVE PROTECTION tokens. These tokens are emitted by StringBuilderEngine as
     * protection/exclusion terms (e.g. `!background`, `!ultrabeast`) — a wrong form silently
     * breaks the protection, the kind of token that must work to keep a valuable Pokémon out of
     * a cleanup/transfer/trade list. Because their exact spacing/form is unverified against a live
     * Turkish client, NONE are emitted: generated strings use the English token (English fallback),
     * mirroring the `count` policy. The candidates below remain as hypotheses to verify live and
     * are shown in the Knowledge Base descriptions. Each value is the current phrase candidate;
     * the no-space alternative is documented in the verification matrix.
     */
    val compoundCandidates: Map<String, String> = mapOf(
        "background" to "arka planlı",
        "locationbackground" to "konum arka planlı",
        "specialbackground" to "özel arka planlı",
        "ultrabeast" to "ultra canavar"
    )

    /**
     * v0.5.5 (Fix 4): the canonical `count` token metadata. `turkish` is null because no
     * candidate is emitted (English fallback); the candidates live in [COUNT_CANDIDATES] and
     * the notes, and in the verification matrix.
     */
    val countMeta: SearchTokenMetadata = SearchTokenMetadata(
        english = "count",
        turkish = null,
        status = TokenVerification.UNTESTED,
        languageSensitive = true,
        example = "count2-",
        notes = "Parser-sensitive numeric syntax (countN-). Turkish candidates contest across " +
            "sources (${COUNT_CANDIDATES.joinToString("/")}); none verified. English 'count' is " +
            "emitted even in Turkish output (English fallback) until a candidate is confirmed live."
    )

    val tokens: List<SearchTokenMetadata> = listOf(
        SearchTokenMetadata("shiny", "parlak", TokenVerification.BETA, true, "shiny", "shiny (English always works; localized form is unverified)"),
        SearchTokenMetadata("traded", "takaslanan", TokenVerification.BETA, true, "!traded", "takas edilmiş (KB description) vs takaslanan (map) — contesting candidates"),
        countMeta,
        SearchTokenMetadata("favorite", "favori", TokenVerification.BETA, true, "favorite", "favourite (UK spelling) is also accepted by the game; localized form unverified"),
        SearchTokenMetadata("lucky", "şanslı", TokenVerification.BETA, true, "lucky"),
        SearchTokenMetadata("legendary", "efsanevi", TokenVerification.BETA, true, "legendary"),
        SearchTokenMetadata("mythical", "mistik", TokenVerification.RISKY, true, "mythical", "Candidate 'mistik' unconfirmed"),
        SearchTokenMetadata("shadow", "gölge", TokenVerification.BETA, true, "shadow"),
        SearchTokenMetadata("purified", "arıtılmış", TokenVerification.RISKY, true, "purified", "Candidate 'arıtılmış' unconfirmed"),
        SearchTokenMetadata("costume", "kostümlü", TokenVerification.BETA, true, "costume"),
        // --- Compound/background/ultra-beast candidates: multi-word, RISKY, unverified. ---
        // v0.5.5 safety hotfix: these are PARSER-SENSITIVE PROTECTION tokens. A wrong multi-word
        // form silently breaks a protection that must work to keep a valuable Pokémon out of a
        // cleanup/transfer/trade list. They are NOT emitted in Turkish output (English fallback) —
        // `turkish` is null. Candidate forms live in [compoundCandidates] and the KB/matrix for
        // verification, not as emitted tokens. Do NOT promote until a candidate is confirmed live.
        SearchTokenMetadata("background", null, TokenVerification.UNTESTED, true, "background", "Multi-word Turkish candidate unverified; English token emitted as protection (fallback)", "Compound candidate '${compoundCandidates["background"]}'; spacing/form unverified. Candidate kept in compoundCandidates, NOT emitted."),
        SearchTokenMetadata("specialbackground", null, TokenVerification.UNTESTED, true, "specialbackground", "Multi-word Turkish candidate unverified; English token emitted as protection (fallback)", "Compound candidate '${compoundCandidates["specialbackground"]}'; spacing/form unverified. Candidate kept in compoundCandidates, NOT emitted."),
        SearchTokenMetadata("locationbackground", null, TokenVerification.UNTESTED, true, "locationbackground", "Multi-word Turkish candidate unverified; English token emitted as protection (fallback)", "Compound candidate '${compoundCandidates["locationbackground"]}'; spacing/form unverified. Candidate kept in compoundCandidates, NOT emitted."),
        SearchTokenMetadata("ultrabeast", null, TokenVerification.UNTESTED, true, "ultrabeast", "Multi-word Turkish candidate unverified; English token emitted as protection (fallback)", "Compound candidate '${compoundCandidates["ultrabeast"]}'; spacing/form unverified. Candidate kept in compoundCandidates, NOT emitted."),
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

    /**
     * v0.5.5 (Fix 4): the compound tokens most likely to be mishandled by a Pokémon GO parser
     * (multi-word localized candidates whose exact spacing/form is unverified). Surfaced so the
     * verification matrix and tests can require these to be tracked and never marked verified.
     *
     * v0.5.5 safety hotfix: these are now UNTESTED + not emitted (English fallback) because a
     * broken protection token is dangerous. Their candidate forms live in [compoundCandidates].
     */
    val compoundTokens: List<SearchTokenMetadata> = listOf(
        byEnglish("specialbackground")!!,
        byEnglish("locationbackground")!!,
        byEnglish("ultrabeast")!!,
        byEnglish("background")!!
    )
}
