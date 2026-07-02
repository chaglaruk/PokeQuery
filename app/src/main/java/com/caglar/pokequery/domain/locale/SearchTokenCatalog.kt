package com.caglar.pokequery.domain.locale

/**
 * Transliteration catalog for Pokémon GO search tokens.
 *
 * English tokens are the default safe output. Turkish tokens are BETA/UNTESTED/RISKY
 * and are NOT emitted in generated search strings unless the user explicitly opts into
 * Turkish output via Settings → Search & Language.
 *
 * v0.7 verification backlog: remaining Turkish localization work
 *   1. Verify `count` candidates (toplam/sayı/sayısı) against a live Turkish Pokémon GO client
 *   2. Verify compound protection tokens (background, locationbackground, specialbackground, ultrabeast)
 *   3. Live-verify all BETA tokens: parlak, efsanevi, gölge, favori, şanslı, takaslanan, kostümlü,
 *      yaş, mesafe, saldırı, savunma, can
 *   4. Resolve RISKY tokens: mistik (mythical), arıtılmış (purified)
 *   5. Add full translations for GamePress-style token examples in Knowledge Base
 * See: docs/localization/turkish_verification_matrix.md
 */
data class LanguageToken(
    val language: String,
    val token: String,
    val notes: String = ""
)

object SearchTokenCatalog {
    val supportedLanguages = listOf("English", "Turkish")

    val tokens: Map<String, List<LanguageToken>> = mapOf(
        "shiny" to listOf(
            LanguageToken("English", "shiny"),
            LanguageToken("Turkish", "parlak", "BETA — community-sourced")
        ),
        "legendary" to listOf(
            LanguageToken("English", "legendary"),
            LanguageToken("Turkish", "efsanevi", "BETA — community-sourced")
        ),
        "mythical" to listOf(
            LanguageToken("English", "mythical"),
            LanguageToken("Turkish", "mistik", "BETA — RISKY")
        ),
        "shadow" to listOf(
            LanguageToken("English", "shadow"),
            LanguageToken("Turkish", "gölge", "BETA — community-sourced")
        ),
        "purified" to listOf(
            LanguageToken("English", "purified"),
            LanguageToken("Turkish", "arıtılmış", "BETA — RISKY")
        ),
        "favorite" to listOf(
            LanguageToken("English", "favorite"),
            LanguageToken("Turkish", "favori", "BETA — community-sourced")
        ),
        "lucky" to listOf(
            LanguageToken("English", "lucky"),
            LanguageToken("Turkish", "şanslı", "BETA — community-sourced")
        ),
        "traded" to listOf(
            LanguageToken("English", "traded"),
            LanguageToken("Turkish", "takaslanan", "BETA — contested: KB says 'Takas edilmiş'")
        ),
        "costume" to listOf(
            LanguageToken("English", "costume"),
            LanguageToken("Turkish", "kostümlü", "BETA — community-sourced")
        ),
        "age" to listOf(
            LanguageToken("English", "age"),
            LanguageToken("Turkish", "yaş", "BETA — community-sourced")
        ),
        "distance" to listOf(
            LanguageToken("English", "distance"),
            LanguageToken("Turkish", "mesafe", "BETA — community-sourced")
        ),
        "attack" to listOf(
            LanguageToken("English", "attack"),
            LanguageToken("Turkish", "saldırı", "BETA — community-sourced")
        ),
        "defense" to listOf(
            LanguageToken("English", "defense"),
            LanguageToken("Turkish", "savunma", "BETA — community-sourced")
        ),
        "hp" to listOf(
            LanguageToken("English", "hp"),
            LanguageToken("Turkish", "can", "BETA — per user request, variable in KB")
        ),
        "count" to listOf(
            LanguageToken("English", "count"),
            LanguageToken("Turkish", "(none)", "UNTESTED — candidates: toplam/sayı/sayısı, none verified")
        ),
        "background" to listOf(
            LanguageToken("English", "background"),
            LanguageToken("Turkish", "arka planlı", "UNTESTED — compound token")
        ),
        "locationbackground" to listOf(
            LanguageToken("English", "locationbackground"),
            LanguageToken("Turkish", "konum arka planlı", "UNTESTED — compound token")
        ),
        "specialbackground" to listOf(
            LanguageToken("English", "specialbackground"),
            LanguageToken("Turkish", "özel arka planlı", "UNTESTED — compound token")
        ),
        "ultrabeast" to listOf(
            LanguageToken("English", "ultrabeast"),
            LanguageToken("Turkish", "ultra canavar", "UNTESTED — compound token")
        )
    )

    fun tokenFor(token: String, language: String): LanguageToken? =
        tokens[token]?.firstOrNull { it.language == language }

    fun isVerified(token: String, language: String): Boolean {
        val entry = tokenFor(token, language) ?: return false
        return !entry.notes.contains("BETA", ignoreCase = true) &&
            !entry.notes.contains("UNTESTED", ignoreCase = true) &&
            !entry.notes.contains("RISKY", ignoreCase = true)
    }

    fun unverifiedTokens(language: String): List<String> =
        tokens.filter { (_, langTokens) ->
            val entry = langTokens.firstOrNull { it.language == language }
            entry != null && (entry.notes.contains("BETA") || entry.notes.contains("UNTESTED") || entry.notes.contains("RISKY"))
        }.keys.toList()

    val betaTokensTurkish: List<String> = listOf(
        "parlak", "efsanevi", "mistik", "gölge", "arıtılmış",
        "favori", "şanslı", "takaslanan", "kostümlü",
        "yaş", "mesafe", "saldırı", "savunma", "can"
    )
}
