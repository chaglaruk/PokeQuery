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

    private enum class ControlPolarity { POSITIVE, NEGATIVE }

    private val clauseBreak = Regex("""\b(?:and|or|ve|veya)\b|[,&;:]""")
    private val contrastRegex = Regex("""\b(?:but|ama|ancak|fakat|lakin)\b""")

    private val negatorPrefix = """(?:don'?t|do\s+not|doesn'?t|does\s+not|isn'?t|is\s+not|aren'?t|are\s+not|can'?t|cannot|won'?t|wont|wouldn'?t|wouldnt)"""
    private val negativeWords = """(?:hide|exclude|without|except|gizle|hariç|haric|dışında|disinda|no|not)"""
    private val positiveWords = """(?:find|show|include|keep|want|get|with|bul|göster|goster|dahil|sakla|ile|birlikte)"""

    private val combinedControlRegex = Regex(
        """\b(?:($negatorPrefix)\s+($negativeWords|$positiveWords)|($negativeWords)|($positiveWords))\b"""
    )

    private val negativeWordSet = setOf(
        "hide", "exclude", "without", "except", "gizle", "hariç", "haric", "dışında", "disinda", "no", "not"
    )

    private val suffixNegations = listOf(
        "degil", "değil", "olmayan", "yok", "hariç", "haric", "disinda", "dışında", "excluded", "hidden"
    )

    private fun isNegativeWord(word: String): Boolean = word.lowercase() in negativeWordSet

    private fun extractLastControl(text: String): ControlPolarity? {
        val matches = combinedControlRegex.findAll(text).toList()
        val last = matches.lastOrNull() ?: return null

        val negator = last.groups[1]?.value
        val negatedWord = last.groups[2]?.value
        val standaloneNeg = last.groups[3]?.value
        val standalonePos = last.groups[4]?.value

        return when {
            negator != null && negatedWord != null -> {
                if (isNegativeWord(negatedWord)) ControlPolarity.POSITIVE
                else ControlPolarity.NEGATIVE
            }
            standaloneNeg != null -> ControlPolarity.NEGATIVE
            standalonePos != null -> ControlPolarity.POSITIVE
            else -> null
        }
    }

    private fun polarityForPrefix(prefix: String): Boolean {
        val trimmedPrefix = prefix.trimEnd()
        if (trimmedPrefix.endsWith("!")) return true

        val contrastMatches = contrastRegex.findAll(prefix).toList()
        if (contrastMatches.isNotEmpty()) {
            val lastContrast = contrastMatches.last()
            val preContrast = prefix.substring(0, lastContrast.range.first)
            val postContrast = prefix.substring(lastContrast.range.last + 1)

            val postControl = extractLastControl(postContrast)
            if (postControl != null) {
                return postControl == ControlPolarity.NEGATIVE
            }

            val preControl = extractLastControl(preContrast) ?: ControlPolarity.POSITIVE
            return preControl == ControlPolarity.POSITIVE
        }

        val control = extractLastControl(prefix)
        return control == ControlPolarity.NEGATIVE
    }

    /**
     * Uses the polarity derived from controls, negators, and contrast markers before the matched keyword.
     * This handles:
     * - Inverted controls: "don't hide shiny" -> shiny, "don't include shiny" -> !shiny
     * - Contrast: "without shiny but with hundo" -> 4*&!shiny, "show all but shiny" -> !shiny, "hide shiny but hundo" -> 4*&!shiny
     * - List inheritance: "hide shiny and favourites" -> !shiny&!favorite
     * - Independent clauses: "Find hundos and exclude shinies" -> 4*&!shiny
     */
    private fun isPatternNegated(normalized: String, keyword: String): Boolean {
        if (keyword.isBlank()) return false
        val index = normalized.indexOf(keyword)
        if (index == -1) return false

        val prefix = normalized.substring(0, index)
        val suffix = normalized.substring(index + keyword.length)

        val prefixNegated = polarityForPrefix(prefix)

        val suffixClause = suffix.split(clauseBreak).firstOrNull().orEmpty().trim()
        val suffixNegated = suffixNegations.any { neg ->
            suffixClause == neg || suffixClause.startsWith("$neg ") || suffixClause.startsWith(neg)
        }
        return prefixNegated || suffixNegated
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
            explanation = "Finds Pokémon with low attack and high defense/HP IVs, capped at CP 1500 for Great League PvP.",
            limitations = listOf("Rank 1 PvP IV spreads vary per species (some prefer 0/15/15, others 0/14/13). Use an external PvP ranker for exact ranks.", "Does not check evolved forms - a 0/15/15 base form may need CP checking.")
        ),
        IntentPattern(
            keywords = listOf("ultra league pvp", "ultra league candidate", "ultra league", "ultra lig"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp", "cp-2500"),
            explanation = "Finds Pokémon with low attack and high defense/HP IVs, capped at CP 2500 for Ultra League PvP.",
            limitations = listOf("Rank 1 PvP IV spreads vary per species. Use an external PvP ranker for exact ranks.", "Does not check evolved forms.")
        ),
        IntentPattern(
            keywords = listOf("pvp", "pvp iv", "gbl", "battle league", "go battle league", "pvp adayı", "pvp adayi"),
            tokens = listOf("0-1attack", "3-4defense", "3-4hp"),
            explanation = "Finds Pokémon with low attack and high defense/HP IVs (stat product optimization for Great/Ultra League).",
            limitations = listOf("Different species have different rank 1 IV spreads. This is a shortlist, not a guarantee.", "Master League requires 15/15/15 (use Hundo search instead).")
        ),
        IntentPattern(
            keywords = listOf("shiny", "shinies", "parlak", "schillernd", "chromatique", "cromatico", "brillante"),
            tokens = listOf("shiny"),
            explanation = "Filters to show only Shiny Pokémon.",
            limitations = listOf("Shiny search does not distinguish costume, event, or regional variants.", "You can also use !shiny to search for non-Shiny Pokémon.")
        ),
        IntentPattern(
            keywords = listOf("legendary", "legendaries", "legend", "efsanevi", "legendaer", "leggendario"),
            tokens = listOf("legendary"),
            explanation = "Filters to show only Legendary Pokémon.",
            limitations = listOf("Mythical Pokémon are NOT included in this search.")
        ),
        IntentPattern(
            keywords = listOf("mythical", "mythicals", "mitik", "mytisch", "mítico", "mitico"),
            tokens = listOf("mythical"),
            explanation = "Filters to show only Mythical Pokémon.",
            limitations = listOf("Mythical Pokémon cannot be traded (except Meltan/Melmetal).")
        ),
        IntentPattern(
            keywords = listOf("shadow", "shadows", "gölge", "golge", "erloest", "obscur", "ombra"),
            tokens = listOf("shadow"),
            explanation = "Filters to show only Shadow Pokémon.",
            limitations = listOf("Shadow Pokémon cannot be traded.", "Shadow Pokémon deal +20% damage but take +20% defense penalty.")
        ),
        IntentPattern(
            keywords = listOf("purified", "arındırılmış", "arindirilmis", "purifie", "purificato"),
            tokens = listOf("purified"),
            explanation = "Filters to show only Purified Pokémon.",
            limitations = listOf("Purified Pokémon cost less candy and stardust to power up.")
        ),
        IntentPattern(
            keywords = listOf("costume", "costumes", "kostüm", "kostum", "costumato"),
            tokens = listOf("costume"),
            explanation = "Filters to show only Costume/Event Pokémon.",
            limitations = listOf("Some costume Pokémon cannot be evolved.")
        ),
        IntentPattern(
            keywords = listOf("favorite", "favorites", "favourite", "favourites", "fav", "favori", "favorit", "favorito"),
            tokens = listOf("favorite"),
            explanation = "Filters to show only Favorited Pokémon.",
            limitations = listOf("Favorites cannot be transferred.")
        ),
        IntentPattern(
            keywords = listOf("lucky", "şanslı", "sansli", "gluecklich", "chanceux", "fortunato"),
            tokens = listOf("lucky"),
            explanation = "Filters to show only Lucky Pokémon.",
            limitations = listOf("Lucky Pokémon cost 50% less stardust to power up.")
        ),
        IntentPattern(
            keywords = listOf("cleanup", "clean up", "transfer", "trash", "junk", "clear space", "box cleanup", "temizlik", "temizle", "silme", "sil"),
            tokens = listOf("0*", "1*"),
            exclusions = listOf("shiny", "legendary", "mythical", "ultrabeast", "costume", "background", "locationbackground", "specialbackground", "shadow", "purified", "favorite", "lucky", "#", "traded", "4*"),
            explanation = "Builds a safe transfer candidate search (0* & 1* IV bands). Excludes all protected categories.",
            limitations = listOf("0* and 1* are IV bands (0-65%), not exact appraisals.", "Always spot-check results before mass-transferring.")
        ),
        IntentPattern(
            keywords = listOf("0*", "0 star", "zero star", "0 yıldız", "0 yildiz"),
            tokens = listOf("0*"),
            explanation = "Finds Pokémon in the 0-star appraisal band (0-49% total IVs).",
            limitations = listOf("0* includes 0/0/0 (nundo) — lock/tag rare 0% Pokémon before transferring.")
        ),
        IntentPattern(
            keywords = listOf("1*", "1 star", "one star", "1 yıldız", "1 yildiz"),
            tokens = listOf("1*"),
            explanation = "Finds Pokémon in the 1-star appraisal band (50-64% total IVs).",
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

        val allTokens = mutableSetOf<String>()
        val allExclusions = mutableSetOf<String>()
        val explanations = mutableListOf<String>()
        val allLimitations = mutableListOf<String>()

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
        }

        if (allTokens.isEmpty() && allExclusions.isEmpty()) {
            val combinedExplanation = explanations.distinct().joinToString(" ")
            return ParsedIntent(emptyList(), explanation = combinedExplanation, limitations = allLimitations.distinct(), canBuild = false)
        }

        val canBuildResult = allTokens.isNotEmpty() || allExclusions.isNotEmpty()
        val combinedExplanation = explanations.distinct().joinToString(" ")
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
