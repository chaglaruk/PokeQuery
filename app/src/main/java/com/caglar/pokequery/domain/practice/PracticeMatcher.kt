package com.caglar.pokequery.domain.practice

/**
 * v0.6.1 — Practice Mode conceptual matcher.
 *
 * A SIMPLIFIED, deterministic matcher based on the app's own generated-string model. It does
 * NOT attempt to fully emulate the Pokémon GO parser. It splits a query on '&' / ',' into
 * positive selectors and negative exclusions (`!token`), then classifies each fake item as:
 *   - PROTECTED  : matches a `!token` exclusion (a protected category kept OUT)
 *   - MATCHED    : matches the positive selector and no exclusion
 *   - NOT_MATCHED: doesn't match the positive selector (and isn't protected)
 *
 * PROTECTED vs EXCLUDED: in this teaching model both mean "kept out by an exclusion", so we
 * report PROTECTED for the first exclusion that fires (the most pedagogically useful framing),
 * and reserve EXCLUDED for future use. Items that hit NO clause at all are NOT_MATCHED.
 *
 * Tokens understood mirror the protection vocabulary the app generates:
 * shiny, legendary, mythical, ultrabeast, costume, background, locationbackground,
 * specialbackground, shadow, purified, favorite, lucky, traded, # (tagged), 4* (hundo),
 * and IV-band positive selectors (0*,1*, hundo check via 4*).
 */
object PracticeMatcher {

    private val flagForToken: Map<String, FakeInventoryPokemon.() -> Boolean> = mapOf(
        "shiny" to { shiny },
        "legendary" to { legendary },
        "mythical" to { mythical },
        "ultrabeast" to { ultrabeast },
        "costume" to { costume },
        "background" to { background },
        "locationbackground" to { locationbackground },
        "specialbackground" to { specialbackground },
        "shadow" to { shadow },
        "purified" to { purified },
        "favorite" to { favorite },
        "lucky" to { lucky },
        "traded" to { traded },
        "#" to { tagged }
    )

    fun match(query: String, items: List<FakeInventoryPokemon> = PracticeDataset.items): List<PracticeMatchResult> {
        // Normalize: split on & or , . Keep clause text including a leading '!' for exclusions.
        val clauses = query.split('&', ',').map { it.trim() }.filter { it.isNotEmpty() }
        val exclusions = clauses.filter { it.startsWith("!") }.map { it.removePrefix("!") }
        val positives = clauses.filterNot { it.startsWith("!") }

        return items.map { item -> classify(item, positives, exclusions) }
    }

    private fun classify(
        item: FakeInventoryPokemon,
        positives: List<String>,
        exclusions: List<String>
    ): PracticeMatchResult {
        // 1) Protected categories (exclusions) take priority: if the item matches any excluded
        //    token, it is PROTECTED (kept out on purpose).
        for (token in exclusions) {
            val reason = matchesExclusion(item, token)
            if (reason != null) {
                return PracticeMatchResult(item, PracticeStatus.PROTECTED, listOf(reason))
            }
        }

        // 2) Positive selector match.
        val (matched, reasons) = matchesPositive(item, positives)
        return if (matched) {
            PracticeMatchResult(item, PracticeStatus.MATCHED, reasons)
        } else {
            PracticeMatchResult(item, PracticeStatus.NOT_MATCHED, reasons.ifEmpty { listOf("Does not match the query's selectors.") })
        }
    }

    private fun matchesExclusion(item: FakeInventoryPokemon, token: String): String? {
        flagForToken[token]?.let { getter ->
            return if (getter(item)) "Protected by !$token" else null
        }
        when (token) {
            "4*", "4star" -> if (item.ivTag == IvTag.HUNDO) return "Protected by !4* (hundo)"
        }
        return null
    }

    /**
     * Returns (matched, reasons). If there are NO positive clauses, every non-protected item is
     * considered a MATCH (e.g. a pure-exclusion cleanup base like the duplicate-cleanup preset's
     * `count2-` would have been simplified out; here a bare exclusion-only query matches all
     * unprotected items as a teaching fallback).
     */
    private fun matchesPositive(item: FakeInventoryPokemon, positives: List<String>): Pair<Boolean, List<String>> {
        if (positives.isEmpty()) return true to listOf("No positive selector; all unprotected items shown.")
        val reasons = mutableListOf<String>()
        var anyMatch = false
        for (clause in positives) {
            when {
                clause == "4*" || clause == "4star" -> {
                    if (item.ivTag == IvTag.HUNDO) { anyMatch = true; reasons.add("Matches 4* (hundo)") }
                }
                clause == "0*" || clause == "1*" || clause == "0*,1*" -> {
                    if (item.ivTag == IvTag.LOW_IV) { anyMatch = true; reasons.add("Matches low-IV band") }
                }
                clause == "0attack&0defense&0hp" || clause == "0attack" -> {
                    if (item.ivTag == IvTag.NUNDO) { anyMatch = true; reasons.add("Matches nundo criteria") }
                }
                clause.contains("cp-") -> {
                    val limit = clause.substringAfter("cp-").substringBefore('&').toIntOrNull()
                    if (limit != null && (item.cp ?: 0) <= limit) { anyMatch = true; reasons.add("CP ≤ $limit") }
                }
                flagForToken[clause] != null -> {
                    if (flagForToken[clause]!!(item)) { anyMatch = true; reasons.add("Matches $clause") }
                }
                // IV-band attack/defense/hp selectors — approximate as PvP candidate for teaching.
                clause.startsWith("0-1attack") || clause.startsWith("3-4defense") || clause.startsWith("3-4hp") -> {
                    if (item.ivTag == IvTag.PVP_CANDIDATE) { anyMatch = true; reasons.add("Matches PvP candidate band") }
                }
            }
        }
        // Comma-within-positive groups are OR; anyMatch across clauses is correct.
        return anyMatch to reasons
    }
}
