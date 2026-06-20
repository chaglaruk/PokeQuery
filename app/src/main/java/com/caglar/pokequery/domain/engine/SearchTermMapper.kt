package com.caglar.pokequery.domain.engine

object SearchTermMapper {

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
        "age" to "yaş",
        "count" to "toplam"
    )

    fun translateSyntax(rawSyntax: String, language: String): String {
        val resolvedLanguage = if (language == "Auto" || language.isBlank()) {
            if (java.util.Locale.getDefault().language == "tr") "Turkish" else "English"
        } else {
            language
        }
        
        if (resolvedLanguage != "Turkish" || rawSyntax.isBlank()) return rawSyntax
        
        // We need to replace words safely. 
        // Example: "count2-&!traded" -> "count2-&!takas edilmiş"
        // "distance100-&!shiny" -> "mesafe100-&!parlak"
        
        var translated = rawSyntax
        
        // We will do a safe replacement to avoid substring clashing
        // Sort keys by length descending so we match longest prefixes first
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
