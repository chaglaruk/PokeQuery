package com.caglar.pokequery.domain.locale

/**
 * Central Turkish localization metadata and confidence registry for Pokémon GO search-string tokens.
 *
 * For every canonical English token (e.g. `shiny`, `costume`, `count`), this registry tracks its
 * Turkish candidate and, critically, HOW TRUSTWORTHY that candidate is.
 *
 * We never fake verification. Every token starts as [TokenVerification.UNTESTED] or
 * [TokenVerification.BETA] and is only promoted to [TokenVerification.VERIFIED] after a
 * human confirms the token's behavior against a live localized Pokémon GO client.
 *
 * Architecture note: [com.caglar.pokequery.domain.engine.SearchTermMapper] is the active emitter
 * implementing search-string syntax translation. This registry is the metadata, documentation,
 * and audit layer that tracks confidence levels, caveats, and unverified fallback tokens.
 */

/** How thoroughly a token's localized form has been checked. Ordered worst to best. */
enum class TokenVerification {
    /** No sufficiently reliable supported localized candidate; fallback remains. */
    UNTESTED,
    /** Known ambiguity, parser concern, or conflicting evidence across sources. */
    RISKY,
    /** Current official Niantic Help Center documents the localized form, but it has not been independently confirmed in a live localized client. */
    BETA,
    /** A human confirmed the token works in a live localized Pokémon GO client. */
    VERIFIED
}

/**
 * Metadata for one search token.
 *
 * @property english            The canonical English token used by default / safe output.
 * @property turkish            The Turkish candidate, or null if none is known or emitted.
 * @property status             Verification status. Drives KB badges and copy policy.
 * @property languageSensitive  True if the localized client is known/expected to need a
 *                              translated form (so getting it wrong silently returns no results).
 * @property example            A concrete search-string example using this token.
 * @property commonMistake      A frequently-wrong substitution, for the KB "Common mistake" row.
 * @property notes              Free-form caveats / contesting candidates.
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
    /** True only when this token has been confirmed in a live localized Pokémon GO client. */
    val isLiveVerified: Boolean get() = status == TokenVerification.VERIFIED
}

/**
 * The registry of Pokémon GO search tokens with Turkish-localization metadata.
 */
object SearchTokenRegistry {

    /**
     * Contesting Turkish candidates for `count` gathered in ONE place.
     * None are verified against a live Turkish client. The mapper uses the English `count` fallback.
     */
    val COUNT_CANDIDATES: List<String> = listOf("toplam", "sayı", "sayısı")

    /**
     * Compound Turkish candidates documented in official Niantic Help Center materials or community usage.
     */
    val compoundCandidates: Map<String, String> = mapOf(
        "background" to "arkaplan",
        "locationbackground" to "konumarkaplanı",
        "ultrabeast" to "ultracanavar"
    )

    /**
     * The canonical `count` token metadata. `turkish` is null because English fallback is emitted.
     */
    val countMeta: SearchTokenMetadata = SearchTokenMetadata(
        english = "count",
        turkish = null,
        status = TokenVerification.UNTESTED,
        languageSensitive = true,
        example = "count2-",
        notes = "Parser-sensitive numeric syntax (countN-). Turkish candidates contest across " +
            "sources (${COUNT_CANDIDATES.joinToString("/")}); none verified in live client. English 'count' is " +
            "emitted even in Turkish output (English fallback) until a candidate is confirmed live."
    )

    val tokens: List<SearchTokenMetadata> = listOf(
        SearchTokenMetadata("cp", "dg", TokenVerification.BETA, true, "cp1500-", notes = "Official Niantic Help Center term: dg (combat power / dövüş gücü)"),
        SearchTokenMetadata("hp", "sp", TokenVerification.BETA, true, "3-4hp", notes = "Official Niantic Help Center term: sp (health points / sağlık puanı)"),
        SearchTokenMetadata("distance", "mesafe", TokenVerification.BETA, true, "distance100-", notes = "Official Niantic Help Center term: mesafe"),
        SearchTokenMetadata("attack", "saldırı", TokenVerification.BETA, true, "0-1attack", notes = "Official Niantic Help Center term: saldırı"),
        SearchTokenMetadata("defense", "savunma", TokenVerification.BETA, true, "3-4defense", notes = "Official Niantic Help Center term: savunma"),
        SearchTokenMetadata("age", "yaş", TokenVerification.BETA, true, "age365-", notes = "Official Niantic Help Center term: yaş"),
        SearchTokenMetadata("year", "yıl", TokenVerification.BETA, true, "year2020", notes = "Official Niantic Help Center term: yıl"),
        SearchTokenMetadata("shiny", "parlak", TokenVerification.BETA, true, "shiny", notes = "Official Niantic Help Center term: parlak"),
        SearchTokenMetadata("legendary", "efsanevi", TokenVerification.BETA, true, "legendary", notes = "Official Niantic Help Center term: efsanevi"),
        SearchTokenMetadata("mythical", "mitolojik", TokenVerification.BETA, true, "mythical", notes = "Official Niantic Help Center term: mitolojik"),
        SearchTokenMetadata("shadow", "gölge", TokenVerification.BETA, true, "shadow", notes = "Official Niantic Help Center term: gölge"),
        SearchTokenMetadata("purified", "arınmış", TokenVerification.BETA, true, "purified", notes = "Official Niantic Help Center term: arınmış"),
        SearchTokenMetadata("favorite", "favori", TokenVerification.BETA, true, "favorite", notes = "Official Niantic Help Center term: favori"),
        SearchTokenMetadata("lucky", "şanslı", TokenVerification.BETA, true, "lucky", notes = "Official Niantic Help Center term: şanslı"),
        SearchTokenMetadata("costume", "kostüm", TokenVerification.BETA, true, "costume", notes = "Official Niantic Help Center term: kostüm"),
        SearchTokenMetadata("traded", "takas edilen", TokenVerification.BETA, true, "!traded", notes = "Official Niantic Help Center term: takas edilen"),
        SearchTokenMetadata("defender", "savunucu", TokenVerification.BETA, true, "defender", notes = "Official Niantic Help Center context: savunucu"),
        SearchTokenMetadata("background", "arkaplan", TokenVerification.BETA, true, "background", notes = "Official Niantic Help Center term: arkaplan"),
        SearchTokenMetadata("locationbackground", "konumarkaplanı", TokenVerification.BETA, true, "locationbackground", notes = "Official Niantic Help Center term: konumarkaplanı"),
        SearchTokenMetadata("specialbackground", null, TokenVerification.UNTESTED, true, "specialbackground", notes = "Conservative English fallback; no official Help Center Turkish mapping."),
        SearchTokenMetadata("ultrabeast", "ultracanavar", TokenVerification.BETA, true, "ultrabeast", notes = "Official Niantic Help Center term: ultracanavar"),
        SearchTokenMetadata("dynamax", "dinamaks", TokenVerification.BETA, true, "dynamax", notes = "Official Niantic Help Center term: dinamaks"),
        SearchTokenMetadata("gigantamax", "gigantamaks", TokenVerification.BETA, true, "gigantamax", notes = "Official Niantic Help Center term: gigantamaks"),
        SearchTokenMetadata("fusion", "füzyon", TokenVerification.BETA, true, "fusion", notes = "Official Niantic Help Center term: füzyon"),
        SearchTokenMetadata("mega", "mega", TokenVerification.BETA, true, "mega", notes = "Official Niantic Help Center term: mega"),
        SearchTokenMetadata("megaevolve", "megaevrim", TokenVerification.BETA, true, "megaevolve", notes = "Official Niantic Help Center term: megaevrim"),
        SearchTokenMetadata("buddy", "dost", TokenVerification.BETA, true, "buddy", notes = "Official Niantic Help Center term: dost"),
        SearchTokenMetadata("evolve", "evrim", TokenVerification.BETA, true, "evolve", notes = "Official Niantic Help Center term: evrim"),
        SearchTokenMetadata("hypertraining", "hipereğitim", TokenVerification.BETA, true, "hypertraining", notes = "Official Niantic Help Center term: hipereğitim"),
        SearchTokenMetadata("item", "eşya", TokenVerification.BETA, true, "item", notes = "Official Niantic Help Center term: eşya"),
        SearchTokenMetadata("evolvenew", "yenievrim", TokenVerification.BETA, true, "evolvenew", notes = "Official Niantic Help Center term: yenievrim"),
        SearchTokenMetadata("evolvequest", "evrimhedef", TokenVerification.BETA, true, "evolvequest", notes = "Official Niantic Help Center term: evrimhedef"),
        SearchTokenMetadata("tradeevolve", "takasevrim", TokenVerification.BETA, true, "tradeevolve", notes = "Official Niantic Help Center term: takasevrim"),
        SearchTokenMetadata("@special", "@özel", TokenVerification.BETA, true, "@special", notes = "Official Niantic Help Center term: @özel"),
        SearchTokenMetadata("@weather", "@havadurumu", TokenVerification.BETA, true, "@weather", notes = "Official Niantic Help Center term: @havadurumu"),
        SearchTokenMetadata("eggsonly", "sadeceyumurta", TokenVerification.BETA, true, "eggsonly", notes = "Official Niantic Help Center term: sadeceyumurta"),
        SearchTokenMetadata("hatched", "yumurtadançıkmış", TokenVerification.BETA, true, "hatched", notes = "Official Niantic Help Center term: yumurtadançıkmış"),
        countMeta
    )

    /** Lookup by canonical English token. */
    fun byEnglish(token: String): SearchTokenMetadata? =
        tokens.firstOrNull { it.english.equals(token, ignoreCase = true) }

    /** Tokens that are NOT live-verified in a localized Pokémon GO client. */
    fun notLiveVerified(): List<SearchTokenMetadata> = tokens.filterNot { it.isLiveVerified }

    /** Tokens explicitly marked language-sensitive. */
    fun languageSensitive(): List<SearchTokenMetadata> = tokens.filter { it.languageSensitive }

    /** All tokens still requiring live verification before they are promoted to VERIFIED. */
    fun unverifiedOrBeta(): List<SearchTokenMetadata> =
        tokens.filter { it.status != TokenVerification.VERIFIED }

    /**
     * Compound tokens tracked for parser safety.
     */
    val compoundTokens: List<SearchTokenMetadata> = listOf(
        byEnglish("specialbackground")!!,
        byEnglish("locationbackground")!!,
        byEnglish("ultrabeast")!!,
        byEnglish("background")!!
    )
}
