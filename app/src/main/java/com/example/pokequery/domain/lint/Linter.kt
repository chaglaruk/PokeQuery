package com.example.pokequery.domain.lint

import com.example.pokequery.domain.engine.StringBuilderEngine

data class LintWarning(val message: String, val isError: Boolean)

object Linter {
    private val reservedTerms = setOf(
        "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "favorite", "favourite",
        "costume", "background", "locationbackground", "specialbackground", "lucky", "traded", "defender",
        "raid", "remoteraid", "hatched", "research", "gbl", "rocket", "snapshot", "evolve", "evolvenew",
        "megaevolve", "tradeevolve", "dynamax", "gigantamax", "adventureeffect"
    )
    private val riskyCategories = setOf("shiny", "legendary", "mythical", "lucky")

    fun lint(query: String): List<LintWarning> {
        val warnings = mutableListOf<LintWarning>()
        val lower = query.lowercase()
        val tokens = lower.split('&', ',', ';', ':').map(String::trim)

        if ('|' in query) {
            warnings += LintWarning("T3 uncertain operator '|'. Do not use it; use '&' or ',' instead.", true)
        }

        if ("count" in lower) {
            val missing = StringBuilderEngine.COUNT_MANDATORY_PROTECTIONS.filter { "!$it" !in lower }
            if (missing.isNotEmpty()) {
                warnings += LintWarning(
                    "Unsafe count usage; missing mandatory exclusions: ${missing.joinToString { "!$it" }}.",
                    true
                )
            }
            warnings += LintWarning("Count uses Pokédex number and does not distinguish shiny, form, gender, or costume.", false)
        }

        if (tokens.any { it == "0*" } && lower != "0attack&0defense&0hp") {
            warnings += LintWarning("0* is an IV band, not exact 0% IV.", false)
        }

        // PvP candidate strings should not be treated as cleanup strings
        val isPvP = lower.contains("0-1attack") || lower.contains("3-4defense")
        val isTradePrep = lower.contains("age365-") || lower.contains("distance100-")

        val cleanupOrCountOrTrade = ("count" in lower || "trade" in lower || tokens.any { it in setOf("0*", "1*", "2*") }) && !isPvP && !isTradePrep
        
        if (cleanupOrCountOrTrade) {
            riskyCategories.filter { it in tokens }.forEach {
                warnings += LintWarning("Risky inclusion of $it in a cleanup/count/trade search.", true)
            }
        }

        if (isTradePrep) {
            warnings += LintWarning("Trade prep search. Review manually. Valuable Pokémon may appear.", false)
        }

        Regex("#([a-z0-9]+)").findAll(lower).map { it.groupValues[1] }.filter { it in reservedTerms }.forEach {
            warnings += LintWarning("Tag '#$it' collides with the reserved search keyword '$it'.", false)
        }

        mapOf(
            "mega" to "Mega0-",
            "count" to "count2-",
            "dynamax" to "dynamax1-",
            "gigantamax" to "gigantamax1-"
        ).filterKeys { shortcut -> shortcut in tokens }.forEach { (shortcut, expansion) ->
            warnings += LintWarning("Shortcut '$shortcut' expands to '$expansion'. Use an explicit term.", false)
        }

        if (query.any { it.code > 127 }) {
            warnings += LintWarning("T4 localized search terms are unverified; MVP assumes an English game client.", false)
        }
        return warnings.distinct()
    }
}
