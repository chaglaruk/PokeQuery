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

    private val patterns = listOf(
        IntentPattern(
            keywords = listOf("hundo", "perfect", "100%", "15/15/15", "15 15 15", "max iv", "all 15"),
            tokens = listOf("4*"),
            explanation = "Finds Pokémon with perfect 15/15/15 IVs (100% appraisal). Inspection only — does not filter or exclude anything.",
            limitations = listOf("4* also matches purified Pokémon. Check manually if you want non-purified only.", "IV appraisal is an approximation, not exact stats.")
        ),
        IntentPattern(
            keywords = listOf("nundo", "0%", "0/0/0", "0 0 0", "zero iv", "lowest", "minimum iv"),
            tokens = listOf("0attack", "0defense", "0hp"),
            explanation = "Finds Pokémon with 0/0/0 IVs. This is an exact match — only true 0% appraisal shows.",
            limitations = listOf("IV floor events (trades, weather boost, raids) make 0% IV impossible.")
        ),
        IntentPattern(
            keywords = listOf("pvp", "pvp iv", "great league", "ultra league", "pvp candidate", "league"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp"),
            explanation = "Finds Pokémon with PvP-friendly IV spreads (low attack, high defense/HP). Filters for Great & Ultra League CP ranges, not Master League.",
            limitations = listOf("CP ranges are approximate. Not all matches are PvP-relevant — species and moveset matter.", "Does not distinguish Great vs Ultra league. Adjust CP cap manually.")
        ),
        IntentPattern(
            keywords = listOf("shiny", "shinies"),
            tokens = listOf("shiny"),
            explanation = "Filters to show only Shiny Pokémon.",
            limitations = listOf("Shiny search does not distinguish costume, event, or regional variants.", "No, you cannot search for 'not shiny' with a single search string.")
        ),
        IntentPattern(
            keywords = listOf("legendary", "legendaries", "legend"),
            tokens = listOf("legendary"),
            explanation = "Filters to show only Legendary Pokémon.",
            limitations = listOf("Mythical Pokémon are NOT included in this search.")
        ),
        IntentPattern(
            keywords = listOf("mythical", "mythic"),
            tokens = listOf("mythical"),
            explanation = "Filters to show only Mythical Pokémon.",
            limitations = listOf("This is a risky filter — mythical Pokémon are often valuable and cannot be re-obtained easily.")
        ),
        IntentPattern(
            keywords = listOf("shadow", "shadows"),
            tokens = listOf("shadow"),
            explanation = "Filters to show only Shadow Pokémon.",
            limitations = listOf("Shadow Pokémon are expensive to power up and cannot be traded.", "Purified Pokémon are NOT included.")
        ),
        IntentPattern(
            keywords = listOf("purified"),
            tokens = listOf("purified"),
            explanation = "Filters to show only Purified Pokémon.",
            limitations = listOf("Purified cost 20% less stardust to power up but cannot be traded or re-shadowed.")
        ),
        IntentPattern(
            keywords = listOf("lucky"),
            tokens = listOf("lucky"),
            explanation = "Filters to show only Lucky Pokémon (traded for guaranteed high IVs).",
            limitations = listOf("Lucky Pokémon cost 50% less stardust to power up but cannot be traded again.")
        ),
        IntentPattern(
            keywords = listOf("costume", "event", "hat", "bow", "crown"),
            tokens = listOf("costume"),
            explanation = "Filters to show only Costume Pokémon.",
            limitations = listOf("Costume Pokémon cannot evolve (with rare event exceptions).")
        ),
        IntentPattern(
            keywords = listOf("favorite", "fav", "starred"),
            tokens = listOf("favorite"),
            explanation = "Filters to show only your Favorite (starred) Pokémon.",
            limitations = listOf("Cannot search for unfavorited Pokémon with a single positive filter.")
        ),
        IntentPattern(
            keywords = listOf("cleanup", "transfer", "delete", "junk", "trash", "bulk transfer"),
            tokens = listOf("1*"),
            explanation = "Finds low-appraisal Pokémon for cleanup or transfer. Safe Cleanup excludes protected categories by default.",
            limitations = listOf("1* is an IV band (0-50%), not exact 1-star. Always review before transferring.", "Exclude shiny, legendary, mythical, costume, shadow, lucky, and trade-relevant Pokémon.")
        ),
        IntentPattern(
            keywords = listOf("candy", "candy prep", "extra candy", "transfer candy"),
            tokens = listOf("count2-"),
            explanation = "Finds duplicate Pokémon (count >= 2) for candy generation via transfer.",
            limitations = listOf("Mandatory exclusions: shiny, legendary, mythical, shadow, purified, and 4*.", "Count refers to species count, not candy. High count = many transfers needed.")
        ),
        IntentPattern(
            keywords = listOf("trade", "trading", "trade fodder", "duplicate", "extra", "spare"),
            tokens = listOf("count2-"),
            exclusions = listOf("traded"),
            explanation = "Finds duplicate untraded Pokémon (count >= 2) for trade with friends.",
            limitations = listOf("Trade eligibility depends on stardust cost (friendship level). High-value Pokémon still cost more.", "Special trades (legendary, shiny, unregistered) are limited to one per day.")
        ),
        IntentPattern(
            keywords = listOf("old", "older", "age", "2016", "2017", "2018", "vintage"),
            tokens = listOf("age365-"),
            explanation = "Finds Pokémon you have caught/obtained at least 365 days ago (1+ year old).",
            limitations = listOf("Older Pokémon from 2016-2018 have a guaranteed Lucky Trade chance.", "Age is based on catch date, not hatch date.")
        ),
        IntentPattern(
            keywords = listOf("distance", "far", "far away", "overseas", "foreign", "distant"),
            tokens = listOf("distance100-"),
            explanation = "Finds Pokémon traded from 100+ km away. These qualify for distance-based candy bonus on transfer.",
            limitations = listOf("Not all distance Pokémon are tradeable again (already traded).", "Distance resets on each trade — the last trade distance applies.")
        ),
        IntentPattern(
            keywords = listOf("untagged", "no tag", "not tagged", "tagged", "tag"),
            tokens = listOf("!"),
            exclusions = listOf("#"),
            explanation = "Finds untagged Pokémon for tagging and organization.",
            limitations = listOf("The exclamation mark inverts the search: !# shows Pokémon WITHOUT tags.", "If you have never tagged, this matches everything.")
        ),
        IntentPattern(
            keywords = listOf("lucky trade", "lucky friend", "guaranteed lucky"),
            tokens = listOf("age365-"),
            exclusions = listOf("traded"),
            explanation = "Finds older untraded Pokémon for Lucky Trades (guaranteed 12/12/12+ IVs).",
            limitations = listOf("Only Pokémon from 2016-2018 are guaranteed Lucky. Newer ones have a small chance.", "Can only make one Special Trade per day by default.")
        ),
        IntentPattern(
            keywords = listOf("all", "everything", "all pokemon", "show all"),
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
                explanation = "Could not understand \"$text\". Try words like: shiny, hundo, cleanup, trade, pvp, lucky, shadow, old, costume.",
                limitations = listOf("PokeQuery understands common search intents. For complex queries, use the Expert Builder."),
                canBuild = false
            )
        }

        val best = matched.maxByOrNull { pattern ->
            pattern.keywords.sumOf { keyword ->
                Regex(keyword).findAll(normalized).count()
            }
        } ?: return ParsedIntent(emptyList(), explanation = "Try rephrasing.", canBuild = false)

        val tokens = best.tokens.toMutableList()
        val exclusions = best.exclusions.toMutableList()
        val combinedLimitations = best.limitations.toMutableList()

        val hasShiny = normalized.contains("shiny")
        val hasLegendary = normalized.contains("legendary")
        val hasMythical = normalized.contains("mythical")

        if (hasShiny && "shiny" !in tokens.map { it.lowercase() }) {
            tokens.add("shiny")
            combinedLimitations.add("Shiny search added based on your input. Verify before transferring.")
        }
        if (hasLegendary && "legendary" !in tokens.map { it.lowercase() }) {
            tokens.add("legendary")
        }
        if (hasMythical && "mythical" !in tokens.map { it.lowercase() }) {
            tokens.add("mythical")
        }

        if (tokens.isEmpty() && !best.canBuild) {
            return ParsedIntent(emptyList(), explanation = best.explanation, limitations = best.limitations, canBuild = false)
        }

        return ParsedIntent(
            tokens = tokens.distinct(),
            exclusions = exclusions.distinct(),
            rawQuery = tokens.joinToString("&") + exclusions.joinToString("") { "&!$it" },
            explanation = best.explanation,
            limitations = combinedLimitations.distinct()
        )
    }
}
