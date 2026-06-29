package com.caglar.pokequery.domain.assist

data class ParsedIntent(
    val tokens: List<String>,
    val exclusions: List<String> = emptyList(),
    val rawQuery: String = tokens.joinToString(" ") { it },
    val explanation: String = "",
    val limitations: List<String> = emptyList(),
    val canBuild: Boolean = tokens.isNotEmpty()
)

data class IntentPattern(
    val keywords: List<String>,
    val tokens: List<String>,
    val exclusions: List<String> = emptyList(),
    val explanation: String,
    val limitations: List<String> = emptyList(),
    val canBuild: Boolean = true
)

object SearchIntentParser {
    private fun normalize(text: String): String =
        text.lowercase().trim().replace(Regex("\\s+"), " ")

    private fun isPatternNegated(normalized: String, keyword: String): Boolean {
        if (keyword.isBlank()) return false
        val index = normalized.indexOf(keyword)
        if (index == -1) return false
        val prefix = normalized.substring(0, index)
        val suffix = normalized.substring(index + keyword.length)
        if (normalized.contains("hide") || normalized.contains("exclude") || normalized.contains("without") || normalized.contains("gizle") || normalized.contains("hariç") || normalized.contains("haric") || normalized.contains("dışında") || normalized.contains("disinda")) return true

        val prefixNegations = listOf("not", "no", "!", "non")
        val suffixNegations = listOf("değil", "degil", "olmayan", "yok")

        val prefixMatch = prefixNegations.any { neg ->
            prefix.trim().endsWith(neg) || prefix.contains("$neg ")
        }
        val suffixMatch = suffixNegations.any { neg ->
            suffix.trim().startsWith(neg) || suffix.contains(" $neg")
        }
        return prefixMatch || suffixMatch
    }

    private val patterns = listOf(
        IntentPattern(
            keywords = listOf("hundo", "perfect", "100%", "15/15/15", "15 15 15", "max iv", "all 15", "yüzde yüz", "yuzde yuz", "100 iv", "kusursuz", "mükemmel", "mukemmel", "güçlü", "guclu"),
            tokens = listOf("4*"),
            explanation = "Finds Pokémon with perfect 15/15/15 IVs (exact 100% appraisal using 4*). Inspection only — does not filter or exclude anything.",
            limitations = listOf("4* also matches purified Pokémon. Check manually if you want non-purified only.", "IV appraisal is an approximation, not exact stats.")
        ),
        IntentPattern(
            keywords = listOf("nundo", "0%", "0/0/0", "0 0 0", "zero iv", "lowest", "minimum iv", "sıfır iv", "sifir iv", "0 iv", "en düşük", "en dusuk"),
            tokens = listOf("0attack", "0defense", "0hp"),
            explanation = "Finds Pokémon with 0/0/0 IVs. This is an exact match — only true 0% appraisal shows.",
            limitations = listOf("IV floor events (trades, weather boost, raids) make 0% IV impossible.")
        ),
        IntentPattern(
            keywords = listOf("great league pvp", "great league candidate", "great league", "büyük lig", "buyuk lig"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp", "cp-1500"),
            explanation = "Finds Great League PvP candidates (CP <= 1500) using CP cap/shortlist logic.",
            limitations = listOf("CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.", "Not all matches are PvP-relevant — species and moveset also matter.")
        ),
        IntentPattern(
            keywords = listOf("ultra league pvp", "ultra league candidate", "ultra league", "ultra lig"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp", "cp-2500"),
            explanation = "Finds Ultra League PvP candidates (CP <= 2500) using CP cap/shortlist logic.",
            limitations = listOf("CP cap filters by current CP only; exact PvP rank and level are not detectable via search strings.", "Not all matches are PvP-relevant — species and moveset also matter.")
        ),
        IntentPattern(
            keywords = listOf("pvp", "pvp iv", "pvp candidate", "pvp adayı", "pvp adayi", "kapışma", "kapisma", "düello", "duello"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp"),
            explanation = "Finds Pokémon with PvP-friendly IV spreads (low attack, high defense/HP). Suitable for Great League and Ultra League — exact PvP rank is not detectable via search strings; check CP manually in Pokémon GO.",
            limitations = listOf("Pokémon GO search cannot detect exact PvP rank or level — only IV floor/ceil values.", "Not all matches are PvP-relevant — species and moveset also matter.", "Does not apply a league CP cap; use specific league name for cap.")
        ),
        IntentPattern(
            keywords = listOf("shiny", "shinies", "parlak", "şayni", "sayni"),
            tokens = listOf("shiny"),
            explanation = "Filters to show only Shiny Pokémon.",
            limitations = listOf("Shiny search does not distinguish costume, event, or regional variants.", "You can also use !shiny to search for non-Shiny Pokémon.")
        ),
        IntentPattern(
            keywords = listOf("legendary", "legendaries", "legend", "efsane", "efsanevi"),
            tokens = listOf("legendary"),
            explanation = "Filters to show only Legendary Pokémon.",
            limitations = listOf("Mythical Pokémon are NOT included in this search.")
        ),
        IntentPattern(
            keywords = listOf("mythical", "mythic", "mitolojik", "gizemli"),
            tokens = listOf("mythical"),
            explanation = "Filters to show only Mythical Pokémon.",
            limitations = listOf("This is a risky filter — mythical Pokémon are often valuable and cannot be re-obtained easily.")
        ),
        IntentPattern(
            keywords = listOf("shadow", "shadows", "gölge", "golge", "karanlık", "karanlik"),
            tokens = listOf("shadow"),
            explanation = "Filters to show only Shadow Pokémon.",
            limitations = listOf("Shadow Pokémon are expensive to power up and cannot be traded.", "Purified Pokémon are NOT included.")
        ),
        IntentPattern(
            keywords = listOf("purified", "arınmış", "arinmis", "temizlenmiş", "temizlenmis"),
            tokens = listOf("purified", "arınmış", "arinmis", "temizlenmiş", "temizlenmis"),
            explanation = "Filters to show only Purified Pokémon.",
            limitations = listOf("Purified Pokémon cost 20% less stardust to power up.", "Purified Pokémon can be traded — they are not blocked from trading.", "Purified Pokémon cannot be re-shadowed.")
        ),
        IntentPattern(
            keywords = listOf("lucky", "şanslı", "sansli"),
            tokens = listOf("lucky", "şanslı", "sansli"),
            explanation = "Filters to show only Lucky Pokémon (received via trade with guaranteed higher IVs).",
            limitations = listOf("Lucky Pokémon cost 50% less stardust to power up.", "Lucky Pokémon cannot be traded again.", "A Pokémon becoming Lucky is not guaranteed — it depends on trade context, not just age or distance.")
        ),
        IntentPattern(
            keywords = listOf("costume", "event", "hat", "bow", "crown", "kostüm", "kostum", "şapka", "sapka", "etkinlik"),
            tokens = listOf("costume"),
            explanation = "Filters to show only Costume Pokémon.",
            limitations = listOf("Costume Pokémon cannot evolve (with rare event exceptions).")
        ),
        IntentPattern(
            keywords = listOf("favorite", "fav", "starred", "favourite", "favourites", "favorites", "favori", "yıldızlı", "yildizli"),
            tokens = listOf("favorite"),
            explanation = "Filters to show only your Favorite (starred) Pokémon.",
            limitations = listOf("You can also use !favorite to search for non-favorited Pokémon.")
        ),
        IntentPattern(
            keywords = listOf("cleanup", "transfer", "delete", "junk", "trash", "bulk transfer", "temizlik", "çöp", "cop", "gönder", "gonder"),
            tokens = listOf("1*"),
            explanation = "Finds low-appraisal Pokémon for cleanup or transfer. Safe Cleanup excludes protected categories by default.",
            limitations = listOf("1* is an IV band (0-50%), not exact 1-star. Always review before transferring.", "Exclude shiny, legendary, mythical, costume, shadow, lucky, and trade-relevant Pokémon.")
        ),
        IntentPattern(
            keywords = listOf("candy", "candy prep", "extra candy", "transfer candy", "şeker", "seker", "şeker için", "seker icin"),
            tokens = listOf("count2-"),
            explanation = "Finds duplicate Pokémon (count >= 2) for candy generation via transfer.",
            limitations = listOf("Mandatory exclusions: shiny, legendary, mythical, shadow, purified, and 4*.", "Count refers to species count, not candy. High count = many transfers needed.")
        ),
        IntentPattern(
            keywords = listOf("trade", "trading", "trade fodder", "duplicate", "extra", "spare", "takas", "ticaret", "takaslık", "takaslik", "fazla"),
            tokens = listOf("count2-"),
            exclusions = listOf("traded"),
            explanation = "Finds duplicate untraded Pokémon (count >= 2) for trade with friends.",
            limitations = listOf("Trade eligibility depends on stardust cost (friendship level). High-value Pokémon still cost more.", "Special trades (legendary, shiny, unregistered) are limited to one per day.")
        ),
        IntentPattern(
            keywords = listOf("old", "older", "age", "2016", "2017", "2018", "vintage", "eski", "yıllık", "yillik", "yaşlı", "yasli"),
            tokens = listOf("age365-"),
            explanation = "Finds Pokémon you have caught/obtained at least 365 days ago (1+ year old).",
            limitations = listOf("Pokémon from 2016-2018 have a higher (but not guaranteed) Lucky Trade chance — a search string cannot prove Lucky eligibility.", "Age is based on catch date, not hatch date.")
        ),
        IntentPattern(
            keywords = listOf("distance", "far", "far away", "overseas", "foreign", "distant", "uzak", "mesafe", "yurtdışı", "yurtdisi", "yurt dışı"),
            tokens = listOf("distance100-"),
            explanation = "Finds Pokémon traded from 100+ km away. These qualify for distance-based candy bonus on transfer.",
            limitations = listOf("Not all distance Pokémon are tradeable again (already traded).", "Distance resets on each trade — the last trade distance applies.")
        ),
        IntentPattern(
            keywords = listOf("untagged", "no tag", "not tagged", "tagged", "tag", "etiketsiz", "etiketlenmemiş", "etiketlenmemis", "etiketlenmeyen", "etiket yok", "etiket"),
            tokens = emptyList(),
            exclusions = listOf("#"),
            explanation = "Finds untagged Pokémon for tagging and organization. The search uses !# (NOT tag filter).",
            limitations = listOf("!# shows Pokémon WITHOUT any tags.", "If you have never tagged, this matches everything.")
        ),
        IntentPattern(
            keywords = listOf("lucky trade", "lucky friend", "guaranteed lucky", "şanslı takas", "sansli takas", "garanti şanslı", "garanti sansli"),
            tokens = listOf("age365-"),
            exclusions = listOf("traded"),
            explanation = "Finds older untraded Pokémon that may qualify for Lucky Trades (12/12/12+ IV floor).",
            limitations = listOf("Only Pokémon from 2016-2018 are guaranteed Lucky. Newer ones have a small chance — a search string cannot prove Lucky eligibility.", "Can only make one Special Trade per day by default.")
        ),
        IntentPattern(
            keywords = listOf("all", "everything", "all pokemon", "show all", "hepsi", "tümü", "tumu", "bütün", "butun"),
            tokens = emptyList(),
            explanation = "Shows all Pokémon. No filter is applied.",
            limitations = listOf("In a large inventory, 'all' may be slow to load. Use filters to narrow down."),
            canBuild = false
        )
    )

    fun parse(text: String): ParsedIntent {
        val normalized = normalize(text)
        if (normalized.isBlank()) return ParsedIntent(emptyList(), explanation = "Enter a description of what you want to find.", canBuild = false)

        val matched = patterns.filter { pattern ->
            pattern.keywords.any { keyword -> normalized.contains(keyword) }
        }

        if (matched.isEmpty()) {
            return ParsedIntent(
                emptyList(),
                explanation = "Could not understand \"$text\". Try words like: shiny, hundo, cleanup, trade, pvp, lucky, shadow, old, costume. (Türkçe: parlak, efsanevi, temizlik, takas, gölge, eski...)",
                limitations = listOf("PokeQuery understands common search intents. For complex queries, use the Expert Builder."),
                canBuild = false
            )
        }

        // Combine ALL matched patterns — not just the "best" one.
        val allTokens = mutableSetOf<String>()
        val allExclusions = mutableSetOf<String>()
        val explanations = mutableListOf<String>()
        val allLimitations = mutableListOf<String>()
        var anyCanBuild = false

        for (pattern in matched) {
            val matchedKeyword = pattern.keywords.firstOrNull { normalized.contains(it) } ?: ""
            val negated = isPatternNegated(normalized, matchedKeyword)

            if (negated) {
                allExclusions.addAll(pattern.tokens)
                allExclusions.addAll(pattern.exclusions)
            } else {
                allTokens.addAll(pattern.tokens)
                allExclusions.addAll(pattern.exclusions)
            }
            explanations.add(pattern.explanation)
            allLimitations.addAll(pattern.limitations)
            if (pattern.canBuild) anyCanBuild = true
        }

        val hasShiny = normalized.contains("shiny")
        val hasLegendary = normalized.contains("legendary")
        val hasMythical = normalized.contains("mythical")

        val extraTokens = buildList {
            if (hasShiny && "shiny" !in allTokens.map { it.lowercase() } && "shiny" !in allExclusions.map { it.lowercase() }) {
                val negated = isPatternNegated(normalized, "shiny")
                if (negated) {
                    allExclusions.add("shiny")
                } else {
                    add("shiny"); allLimitations.add("Shiny search added based on your input. Verify before transferring.")
                }
            }
            if (hasLegendary && "legendary" !in allTokens.map { it.lowercase() } && "legendary" !in allExclusions.map { it.lowercase() }) {
                val negated = isPatternNegated(normalized, "legendary")
                if (negated) allExclusions.add("legendary") else add("legendary")
            }
            if (hasMythical && "mythical" !in allTokens.map { it.lowercase() } && "mythical" !in allExclusions.map { it.lowercase() }) {
                val negated = isPatternNegated(normalized, "mythical")
                if (negated) allExclusions.add("mythical") else add("mythical")
            }
        }
        allTokens.addAll(extraTokens)

        val extraLabel = if (extraTokens.isNotEmpty()) " [Added: ${extraTokens.joinToString(", ")}]" else ""

        if (allTokens.isEmpty() && allExclusions.isEmpty()) {
            val combinedExplanation = explanations.distinct().joinToString(" ") + extraLabel
            return ParsedIntent(emptyList(), explanation = combinedExplanation, limitations = allLimitations.distinct(), canBuild = false)
        }

        // canBuild when there are any tokens OR any exclusions (e.g. untagged → no tokens, !# exclusion)
        val canBuildResult = allTokens.isNotEmpty() || allExclusions.isNotEmpty()

        val combinedExplanation = explanations.distinct().joinToString(" ") + extraLabel

        // Build rawQuery beautifully
        val distinctTokens = allTokens.toList().distinct()
        val distinctExclusions = allExclusions.toList().distinct()
        val parts = distinctTokens + distinctExclusions.map { "!$it" }
        val rawQuery = parts.joinToString("&")

        return ParsedIntent(
            tokens = distinctTokens,
            exclusions = distinctExclusions,
            rawQuery = rawQuery,
            explanation = combinedExplanation,
            limitations = allLimitations.distinct(),
            canBuild = canBuildResult
        )
    }
}
