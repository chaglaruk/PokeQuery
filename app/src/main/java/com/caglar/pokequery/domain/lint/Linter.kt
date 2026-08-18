package com.caglar.pokequery.domain.lint

import com.caglar.pokequery.domain.engine.StringBuilderEngine

data class LintWarning(val message: String, val isError: Boolean)

object Linter {
    private val reservedTerms = setOf(
        "shiny", "legendary", "mythical", "ultrabeast", "shadow", "purified", "favorite", "favourite",
        "costume", "background", "locationbackground", "specialbackground", "lucky", "traded", "defender",
        "raid", "remoteraid", "hatched", "eggsonly", "research", "gbl", "rocket", "snapshot", "evolve", "evolvenew",
        "evolvequest", "megaevolve", "tradeevolve", "hypertraining", "item", "fusion", "dynamax", "gigantamax",
        "adventureeffect"
    )
    private val riskyCategories = setOf("shiny", "legendary", "mythical", "lucky")

    private fun tokens(query: String): List<String> =
        query.lowercase()
            .split('&', '|', ',', ';', ':')
            .map(String::trim)
            .filter(String::isNotBlank)

    fun lint(query: String): List<LintWarning> {
        val warnings = mutableListOf<LintWarning>()
        val lower = query.lowercase()
        val tokens = tokens(query)

        // Current official Pokémon GO Help Center documentation lists both '&' and '|' as
        // supported ways to combine searches matching multiple criteria. Do not reject or
        // rewrite '|'; Event Guide suggested searches keep their separate no-pipe invariant.

        if (tokens.any { it == "!untraded" || it == "untraded" }) {
            warnings += LintWarning("Unsupported token 'untraded'. Use 'traded' to include traded Pokémon or '!traded' to exclude them.", true)
        }

        val hasCount = tokens.any { it.matches(Regex("count\\d*-?")) }
        if (hasCount) {
            val missing = StringBuilderEngine.COUNT_MANDATORY_PROTECTIONS.filter { protection ->
                tokens.none { it == "!$protection" }
            }
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

        val isPvP = tokens.any { it == "0-1attack" || it == "3-4defense" }
        val isTradePrep = tokens.any { it == "age365-" || it == "distance100-" }
        val cleanupOrCount = (hasCount || tokens.any { it in setOf("0*", "1*", "2*") }) && !isPvP && !isTradePrep

        if (cleanupOrCount) {
            riskyCategories.filter { it in tokens }.forEach {
                warnings += LintWarning("Risky inclusion of $it in a cleanup/count search.", true)
            }
        }

        if (isTradePrep) {
            warnings += LintWarning("Trade prep search. Review manually. Valuable Pokémon may appear.", false)
        }

        if (!cleanupOrCount && !isPvP && !isTradePrep) {
            riskyCategories.filter { it in tokens }.forEach {
                warnings += LintWarning("Includes $it as a positive filter. Review matches before acting.", false)
            }
        }

        Regex("#([a-z0-9]+)").findAll(lower).map { it.groupValues[1] }.filter { it in reservedTerms }.forEach {
            warnings += LintWarning("Tag '#$it' collides with the reserved search keyword '$it'.", false)
        }

        mapOf(
            "mega" to "Mega0-",
            "count" to "count2-"
        ).filterKeys { shortcut -> shortcut in tokens }.forEach { (shortcut, expansion) ->
            warnings += LintWarning("Shortcut '$shortcut' expands to '$expansion'. Use an explicit term.", false)
        }

        if (query.any { it.code > 127 }) {
            warnings += LintWarning(
                "Localized search terms detected. Officially documented localized terms may still be beta until independently verified in the live game client.",
                false
            )
        }
        return warnings.distinct()
    }
}
