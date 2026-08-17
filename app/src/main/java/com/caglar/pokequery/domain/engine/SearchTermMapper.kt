package com.caglar.pokequery.domain.engine

object SearchTermMapper {

    // ---------------------------------------------------------------------------
    // Official Help Center-backed search-term maps.
    //
    // Source: Niantic Help Center FAQ 1486, locale paths en/de/es/fr/it/tr.
    // Conservative rule: map terms explicitly documented by official inventory-search
    // pages. Multi-word terms are allowed when the official page itself documents the
    // phrase as the search term (for example Turkish `takas edilen`, Spanish `con suerte`).
    // ---------------------------------------------------------------------------
    private val turkishMap = mapOf(
        "cp" to "dg",
        "hp" to "sp",
        "distance" to "mesafe",
        "attack" to "saldırı",
        "defense" to "savunma",
        "age" to "yaş",
        "year" to "yıl",
        "shiny" to "parlak",
        "legendary" to "efsanevi",
        "mythical" to "mitolojik",
        "shadow" to "gölge",
        "purified" to "arınmış",
        "favorite" to "favori",
        "lucky" to "şanslı",
        "costume" to "kostüm",
        "traded" to "takas edilen",
        "defender" to "savunucu",
        "background" to "arkaplan",
        "locationbackground" to "konumarkaplanı",
        "ultrabeast" to "ultracanavar",
        "dynamax" to "dinamaks",
        "gigantamax" to "gigantamaks",
        "fusion" to "füzyon",
        "mega" to "mega",
        "megaevolve" to "megaevrim",
        "buddy" to "dost",
        "evolve" to "evrim",
        "hypertraining" to "hipereğitim",
        "item" to "eşya",
        "evolvenew" to "yenievrim",
        "evolvequest" to "evrimhedef",
        "tradeevolve" to "takasevrim",
        "@special" to "@özel",
        "@weather" to "@havadurumu",
        "eggsonly" to "sadeceyumurta",
        "hatched" to "yumurtadançıkmış"
    )

    private val germanMap = mapOf(
        "cp" to "wp",
        "hp" to "kp",
        "distance" to "entfernung",
        "attack" to "angriff",
        "defense" to "verteidigung",
        "age" to "alter",
        "year" to "jahr",
        "shiny" to "schillernd",
        "legendary" to "legendär",
        "mythical" to "mysteriös",
        "shadow" to "crypto",
        "purified" to "erlöst",
        "favorite" to "favorit",
        "lucky" to "glücks",
        "costume" to "kostümiert",
        "traded" to "getauscht",
        "defender" to "verteidiger",
        "background" to "hintergrund",
        "locationbackground" to "ortshintergrund",
        "ultrabeast" to "ultrabestie",
        "dynamax" to "dynamax",
        "gigantamax" to "gigadynamax",
        "fusion" to "fusion",
        "mega" to "mega",
        "megaevolve" to "megaentwicklung",
        "buddy" to "kumpel",
        "evolve" to "entwickeln",
        "hypertraining" to "superspezialtraining",
        "item" to "item",
        "evolvenew" to "neueentwicklung",
        "evolvequest" to "entwicklungsaufgabe",
        "tradeevolve" to "tauschentwicklung",
        "@special" to "@spezial",
        "@weather" to "@wetter",
        "eggsonly" to "nurausEiern",
        "hatched" to "ausgebrütet"
    )

    private val spanishMap = mapOf(
        "cp" to "pc",
        "hp" to "ps",
        "distance" to "distancia",
        "attack" to "ataque",
        "defense" to "defensa",
        "age" to "edad",
        "year" to "año",
        "shiny" to "variocolor",
        "legendary" to "legendario",
        "mythical" to "singular",
        "shadow" to "oscuro",
        "purified" to "purificado",
        "favorite" to "favorito",
        "lucky" to "con suerte",
        "costume" to "disfraz",
        "traded" to "intercambiados",
        "defender" to "defensor",
        "background" to "fondo",
        "locationbackground" to "fondolugar",
        "ultrabeast" to "ultraentes",
        "dynamax" to "dinamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusión",
        "mega" to "mega",
        "megaevolve" to "megaevolucionar",
        "buddy" to "compañero",
        "evolve" to "evolucionar",
        "hypertraining" to "entrenamiento extremo",
        "item" to "objeto",
        "evolvenew" to "nuevaevolución",
        "evolvequest" to "misión evolución",
        "tradeevolve" to "evoluciónintercambio",
        "@special" to "@especial",
        "@weather" to "@tiempo atmosférico",
        "eggsonly" to "huevosolo",
        "hatched" to "eclosionado"
    )

    private val frenchMap = mapOf(
        "cp" to "pc",
        "hp" to "pv",
        "distance" to "distance",
        "attack" to "attaque",
        "defense" to "défense",
        "age" to "âge",
        "year" to "année",
        "shiny" to "chromatique",
        "legendary" to "légendaire",
        "mythical" to "fabuleux",
        "shadow" to "obscur",
        "purified" to "purifié",
        "favorite" to "favoris",
        "lucky" to "chanceux",
        "costume" to "costume",
        "traded" to "échangé",
        "defender" to "défenseur",
        "background" to "fond",
        "locationbackground" to "fondlieu",
        "ultrabeast" to "ultra-chimère",
        "dynamax" to "dynamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusion",
        "mega" to "méga",
        "megaevolve" to "mégaévolue",
        "buddy" to "copain",
        "evolve" to "évoluer",
        "hypertraining" to "entraînementultime",
        "item" to "objet",
        "evolvenew" to "nouvelleévolution",
        "evolvequest" to "évolutionparquête",
        "tradeevolve" to "évolutionparéchange",
        "@special" to "@spécial",
        "@weather" to "@météo",
        "eggsonly" to "oeufseulement",
        "hatched" to "éclos"
    )

    private val italianMap = mapOf(
        "cp" to "pl",
        "hp" to "ps",
        "distance" to "distanza",
        "attack" to "attacco",
        "defense" to "difesa",
        "age" to "età",
        "year" to "anno",
        "shiny" to "cromatico",
        "legendary" to "leggendario",
        "mythical" to "misterioso",
        "shadow" to "ombra",
        "purified" to "purificato",
        "favorite" to "preferiti",
        "lucky" to "fortunato",
        "costume" to "costume",
        "traded" to "scambiato",
        "defender" to "difensore",
        "background" to "sfondo",
        "locationbackground" to "sfondodiposizione",
        "ultrabeast" to "ultracreatura",
        "dynamax" to "dynamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusione",
        "mega" to "mega",
        "megaevolve" to "megaevoluto",
        "buddy" to "compagno",
        "evolve" to "fai evolvere",
        "hypertraining" to "allenamentopro",
        "item" to "strumento",
        "evolvenew" to "nuovaevoluzione",
        "evolvequest" to "evoluzionetramitericerca",
        "tradeevolve" to "evoluzionetramitescambio",
        "@special" to "@speciale",
        "@weather" to "@meteo",
        "eggsonly" to "solouovo",
        "hatched" to "dauovo"
    )

    private val knownTokenKeys = setOf(
        "cp", "hp", "attack", "defense", "age", "distance", "year",
        "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified",
        "favorite", "lucky", "traded", "defender", "costume",
        "background", "locationbackground", "specialbackground",
        "mega", "megaevolve", "buddy", "evolve", "hypertraining", "item",
        "evolvenew", "evolvequest", "tradeevolve", "@special", "@weather",
        "eggsonly", "hatched", "dynamax", "gigantamax", "fusion", "count"
    )

    private val turkishChars = charArrayOf('ı', 'ş', 'ğ', 'İ', 'Ş', 'Ğ')

    private val turkishDistinctTokens = turkishMap.values
        .filter { it.isNotBlank() && it != "mega" }
        .sortedByDescending { it.length }

    private val turkishTokenRegexes = turkishDistinctTokens.map { token ->
        Regex("(?<=^|[&!,;:\\s])${Regex.escape(token)}(?=[0-9\\-&,;:\\s]|\$)", RegexOption.IGNORE_CASE)
    }

    fun getMapFor(language: String): Map<String, String> = when (language) {
        "Turkish" -> turkishMap
        "German" -> germanMap
        "Spanish" -> spanishMap
        "French" -> frenchMap
        "Italian" -> italianMap
        else -> emptyMap()
    }

    /**
     * Resolves the effective output language from the stored setting.
     */
    fun resolveLanguage(language: String): String =
        if (language.isBlank() || language.equals("Auto", ignoreCase = true)) "English" else language

    /**
     * Token-boundary-aware heuristic: does this generated search string look like Turkish output?
     */
    fun looksTurkish(rawSyntax: String): Boolean {
        if (rawSyntax.isBlank()) return false
        if (rawSyntax.any { turkishChars.contains(it) }) return true
        return turkishTokenRegexes.any { it.containsMatchIn(rawSyntax) }
    }

    /** Returns any unverified tokens present in the query for the given language. */
    fun findUnverifiedTokens(query: String, language: String): List<String> {
        val resolvedLanguage = resolveLanguage(language)
        if (resolvedLanguage == "English" || query.isBlank()) return emptyList()
        val map = getMapFor(resolvedLanguage)

        // Find all tokens in the query split on standard PokeQuery separators
        val segments = query.split(Regex("[&!,;:|\\s]+"))
            .map { it.replace(Regex("^[!]+"), "").replace(Regex("[0-9\\-*]+$"), "").trim() }
            .filter { it.isNotEmpty() }

        return segments.filter { token -> token in knownTokenKeys && !map.containsKey(token) }.distinct()
    }

    fun translateSyntax(rawSyntax: String, language: String): String {
        val resolvedLanguage = resolveLanguage(language)
        val map = getMapFor(resolvedLanguage)

        if (map.isEmpty() || rawSyntax.isBlank()) return rawSyntax

        var translated = rawSyntax

        // Sort keys by length descending so longer keywords (e.g. megaevolve before mega) match first.
        val keys = map.keys.sortedByDescending { it.length }

        for (key in keys) {
            val tr = map[key]!!

            // Segment boundary matching: preceded by start of string or & ! , ; :
            // and followed by digit, hyphen, separator, or end of string.
            val regex = Regex("(?<=^|[&!,;:])(${Regex.escape(key)})(?=[0-9\\-&,;:]|\$)")
            translated = regex.replace(translated, tr)
        }

        return translated
    }
}
