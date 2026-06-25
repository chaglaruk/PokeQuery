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
    //   - hp           : "can" per explicit user requirement; KB notes variable localization
    //
    // v0.5.5 safety hotfix — compound parser-sensitive tokens fall back to English. The multi-word
    // candidates (background / locationbackground / specialbackground / ultrabeast) are deliberately
    // NOT mapped here. Their exact spacing/form is unverified against a live Turkish client, and a
    // wrong multi-word form silently breaks a PROTECTION token — the kind of token that must work to
    // keep a valuable Pokémon out of a cleanup/transfer/trade list. Until one candidate per token is
    // confirmed live, generated protection strings emit the English token even in Turkish output
    // (English fallback). Candidate forms remain visible in the Knowledge Base descriptions_tr and in
    // SearchTokenRegistry.compoundCandidates as hypotheses to verify. This mirrors the existing
    // `count` English-fallback policy.
    // ---------------------------------------------------------------------------
    private val turkishMap = mapOf(
        "shiny" to "parlak",
        "legendary" to "efsanevi",
        "mythical" to "mistik",
        "shadow" to "gölge",
        "purified" to "arıtılmış",
        "favorite" to "favori",
        "lucky" to "şanslı",
        "traded" to "takaslanan",
        "costume" to "kostümlü",

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
        //
        // NOTE: the compound parser-sensitive protection tokens ("background",
        // "locationbackground", "specialbackground", "ultrabeast") are ALSO deliberately NOT
        // mapped (v0.5.5 safety hotfix). Their multi-word Turkish candidates are unverified and a
        // broken protection token is dangerous. They fall back to English in generated strings.
        // Candidate forms are tracked in SearchTokenRegistry.compoundCandidates.
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
        // Example: "count2-&!traded" -> "count2-&!takaslanan" (count stays English, fallback).
        // "distance100-&!shiny" -> "mesafe100-&!parlak"
        // Protection tokens NOT in the map (background, ultrabeast, ...) are left in English
        // (English fallback) — see the v0.5.5 safety hotfix note above.

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
