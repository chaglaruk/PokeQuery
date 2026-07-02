package com.caglar.pokequery.domain.engine

object SearchTermMapper {

    // ---------------------------------------------------------------------------
    // Official Help Center-backed search-term maps.
    //
    // Source: Niantic Help Center FAQ 1486, locale paths en/de/es/fr/it/tr.
    // Conservative rule: only map single parser-safe tokens visible in the official pages.
    // `count`, `specialbackground`, and multi-word terms such as Turkish/Spanish traded stay English.
    // ---------------------------------------------------------------------------
    private val turkishMap = mapOf(
        "shiny" to "parlak",
        "legendary" to "efsanevi",
        "mythical" to "mitolojik",
        "shadow" to "gölge",
        "purified" to "arınmış",
        "favorite" to "favori",
        "lucky" to "şanslı",
        "costume" to "kostüm",
        "attack" to "saldırı",
        "defense" to "savunma",
        "hp" to "sp",
        "distance" to "mesafe",
        "age" to "yaş",
        "year" to "yıl",
        "evolve" to "evrim",
        "dynamax" to "dinamaks",
        "gigantamax" to "gigantamaks",
        "fusion" to "füzyon",
        "cp" to "dg",
        "defender" to "savunucu",
        "background" to "arkaplan",
        "locationbackground" to "konumarkaplanı",
        "ultrabeast" to "ultracanavar"
    )

    private val germanMap = mapOf(
        "shiny" to "schillernd",
        "legendary" to "legendär",
        "mythical" to "mysteriös",
        "shadow" to "crypto",
        "purified" to "erlöst",
        "favorite" to "favorit",
        "lucky" to "glücks",
        "costume" to "kostümiert",
        "attack" to "angriff",
        "defense" to "verteidigung",
        "hp" to "kp",
        "distance" to "entfernung",
        "age" to "alter",
        "year" to "jahr",
        "evolve" to "entwickeln",
        "dynamax" to "dynamax",
        "gigantamax" to "gigadynamax",
        "fusion" to "fusion",
        "cp" to "wp",
        "defender" to "verteidiger",
        "background" to "hintergrund",
        "locationbackground" to "ortshintergrund",
        "ultrabeast" to "ultrabestie"
    )

    private val spanishMap = mapOf(
        "shiny" to "variocolor",
        "legendary" to "legendario",
        "mythical" to "singular",
        "shadow" to "oscuro",
        "purified" to "purificado",
        "favorite" to "favorito",
        "costume" to "disfraz",
        "attack" to "ataque",
        "defense" to "defensa",
        "hp" to "ps",
        "distance" to "distancia",
        "age" to "edad",
        "year" to "año",
        "evolve" to "evolucionar",
        "dynamax" to "dinamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusión",
        "cp" to "pc",
        "defender" to "defensor",
        "background" to "fondo",
        "locationbackground" to "fondolugar",
        "ultrabeast" to "ultraentes"
    )

    private val frenchMap = mapOf(
        "shiny" to "chromatique",
        "legendary" to "légendaire",
        "mythical" to "fabuleux",
        "shadow" to "obscur",
        "purified" to "purifié",
        "favorite" to "favoris",
        "lucky" to "chanceux",
        "costume" to "costume",
        "attack" to "attaque",
        "defense" to "défense",
        "hp" to "pv",
        "distance" to "distance",
        "age" to "âge",
        "year" to "année",
        "evolve" to "évoluer",
        "dynamax" to "dynamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusion",
        "cp" to "pc",
        "defender" to "défenseur",
        "background" to "fond",
        "locationbackground" to "fondlieu",
        "ultrabeast" to "ultra-chimère"
    )

    private val italianMap = mapOf(
        "shiny" to "cromatico",
        "legendary" to "leggendario",
        "mythical" to "misterioso",
        "shadow" to "ombra",
        "purified" to "purificato",
        "favorite" to "preferiti",
        "lucky" to "fortunato",
        "costume" to "costume",
        "attack" to "attacco",
        "defense" to "difesa",
        "hp" to "ps",
        "distance" to "distanza",
        "age" to "età",
        "year" to "anno",
        "dynamax" to "dynamax",
        "gigantamax" to "gigamax",
        "fusion" to "fusione",
        "cp" to "pl",
        "defender" to "difensore",
        "background" to "sfondo",
        "locationbackground" to "sfondodiposizione",
        "ultrabeast" to "ultracreatura"
    )

    private val knownTokenKeys = setOf(
        "cp", "hp", "attack", "defense", "age", "distance", "year",
        "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified",
        "favorite", "lucky", "traded", "defender", "costume",
        "background", "locationbackground", "specialbackground",
        "mega", "evolve", "dynamax", "gigantamax", "fusion", "count"
    )

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
     * Heuristic: does this generated string look like Turkish output?
     */
    fun looksTurkish(rawSyntax: String): Boolean {
        if (rawSyntax.isBlank()) return false
        if (turkishMap.values.any { it.isNotBlank() && rawSyntax.contains(it, ignoreCase = true) }) return true
        return rawSyntax.any { it.lowercaseChar() in setOf('ı', 'ş', 'ğ', 'İ', 'Ş', 'Ğ') }
    }

    /** Returns any unverified tokens present in the query for the given language. */
    fun findUnverifiedTokens(query: String, language: String): List<String> {
        val resolvedLanguage = resolveLanguage(language)
        if (resolvedLanguage == "English" || query.isBlank()) return emptyList()
        val map = getMapFor(resolvedLanguage)

        // Find all tokens in the query
        val tokens = query.split(Regex("[&!,;:|\\s]+"))
            .map { it.replace(Regex("[0-9\\-*]"), "").trim() }
            .filter { it.isNotEmpty() }

        return tokens.filter { token -> token in knownTokenKeys && !map.containsKey(token) }.distinct()
    }

    fun translateSyntax(rawSyntax: String, language: String): String {
        val resolvedLanguage = resolveLanguage(language)
        val map = getMapFor(resolvedLanguage)

        if (map.isEmpty() || rawSyntax.isBlank()) return rawSyntax

        var translated = rawSyntax

        // Sort keys by length descending so we match longest prefixes first.
        val keys = map.keys.sortedByDescending { it.length }

        for (key in keys) {
            val tr = map[key]!!

            // Regex to match the key as a word/prefix (handles cases like distance100-)
            val regex = Regex("(?<=^|[&!,])($key)(?=[0-9\\-&,]|\$)")
            translated = regex.replace(translated, tr)
        }

        return translated
    }
}
