package com.caglar.pokequery.domain.engine

object SearchTermMapper {

    // ---------------------------------------------------------------------------
    // Turkish search-term map (BETA — verify before relying on these tokens).
    //
    // v0.4.2 safety patch (Fix 3, audit BUG-002/003): Turkish output is treated as
    // BETA. The map below is community-sourced and NOT verified against the live
    // Pokémon GO Turkish client. Per docs/research/turkish_localization_plan.md the
    // spot-check matrix for these terms is still "Pending". Do not enable Auto→Turkish.
    //
    // v0.5.5 (Fix 4) — token truth is centralized and aligned across code/registry/KB/docs:
    //   - count        : CONTESTED across sources ("toplam" in the old map, "sayı" in the
    //                    localization plan, "sayısı" in the KB). It is also parser-sensitive
    //                    numeric syntax (countN-). Until ONE candidate is confirmed live, we
    //                    DO NOT translate it — the English `count` is emitted even in Turkish
    //                    output (English fallback). Candidates remain in the verification
    //                    matrix as hypotheses to test. See SearchTokenRegistry.countMeta.
    //   - traded       : map uses "takaslanan"; KB description_tr "Takas edilmiş" (contested)
    //   - mythical     : map "mistik" — verify
    //   - purified     : map "arıtılmış" — verify
    //   - specialbackground / locationbackground / ultrabeast : multi-word compound candidates
    //                    — unverified; documented as RISKY in the registry + matrix.
    //   - hp           : "can" per explicit user requirement; KB notes variable localization
    // ---------------------------------------------------------------------------
    private val turkishMap = mapOf(
        "shiny" to "parlak",
        "legendary" to "efsanevi",
        "mythical" to "mistik",
        "ultrabeast" to "ultra canavar",
        "shadow" to "gölge",
        "purified" to "arıtılmış",
        "favorite" to "favori",
        "lucky" to "şanslı",
        "traded" to "takaslanan",
        "costume" to "kostümlü",
        "background" to "arka planlı",
        "locationbackground" to "konum arka planlı",
        "specialbackground" to "özel arka planlı",

        // IV and Stats
        "attack" to "saldırı",
        "defense" to "savunma",
        "hp" to "can",

        // Distance and age terms sometimes localize, sometimes not.
        // We map them just in case based on standard localized prefixes.
        "distance" to "mesafe",
        "age" to "yaş"
        // NOTE: "count" is deliberately NOT mapped. See the block comment above — the Turkish
        // candidate is contested (toplam/sayı/sayısı) and the token is parser-sensitive numeric
        // syntax, so the English "count" is emitted even in Turkish output until a candidate is
        // confirmed live. SearchTokenRegistry.COUNT_CANDIDATES lists the hypotheses to test.
    )

    /**
     * Resolves the effective output language from the stored setting.
     *
     * v0.4.2 (Fix 3): "Auto" and blank values resolve to the safe default (English),
     * NOT to the device locale. Turkish is available only as an explicit manual choice
     * because its tokens are unverified (BETA).
     */
    fun resolveLanguage(language: String): String =
        if (language.isBlank() || language.equals("Auto", ignoreCase = true)) "English" else language

    /**
     * Heuristic: does this generated string look like Turkish output?
     * Used by the RiskWarning screen to show the Turkish-beta caution (Fix 3).
     * Detects either any mapped Turkish value or distinct Turkish-only letters.
     */
    fun looksTurkish(rawSyntax: String): Boolean {
        if (rawSyntax.isBlank()) return false
        if (turkishMap.values.any { it.isNotBlank() && rawSyntax.contains(it, ignoreCase = true) }) return true
        // Turkish-specific letters that never appear in English output.
        return rawSyntax.any { it.lowercaseChar() in setOf('ı', 'ş', 'ğ', 'İ', 'Ş', 'Ğ') }
    }

    fun translateSyntax(rawSyntax: String, language: String): String {
        val resolvedLanguage = resolveLanguage(language)

        if (resolvedLanguage != "Turkish" || rawSyntax.isBlank()) return rawSyntax

        // Safe replacement to avoid substring clashing.
        // Example: "count2-&!traded" -> "toplam2-&!takaslanan"
        // "distance100-&!shiny" -> "mesafe100-&!parlak"

        var translated = rawSyntax

        // Sort keys by length descending so we match longest prefixes first.
        val keys = turkishMap.keys.sortedByDescending { it.length }

        for (key in keys) {
            val tr = turkishMap[key]!!

            // Regex to match the key as a word/prefix (handles cases like distance100-)
            // It matches the key if it's preceded by start of string, &, !, or ,
            // and followed by end of string, number, &, or ,
            val regex = Regex("(?<=^|[&!,])($key)(?=[0-9\\-&,]|\$)")
            translated = regex.replace(translated, tr)
        }

        return translated
    }
}
