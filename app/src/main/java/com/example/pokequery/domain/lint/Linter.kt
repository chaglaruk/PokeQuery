package com.example.pokequery.domain.lint

data class LintWarning(val message: String, val isError: Boolean)

object Linter {
    fun lint(query: String): List<LintWarning> {
        val warnings = mutableListOf<LintWarning>()
        if (query.contains("|")) {
            warnings.add(LintWarning("Do not use '|' for OR. Use ',' instead.", isError = true))
        }
        if (query.contains("count") && !query.contains("!shiny")) {
            warnings.add(LintWarning("Unsafe count usage. Does not distinguish shiny/forms.", isError = true))
        }
        if (query.contains("0*") && !query.contains("!0*")) {
            warnings.add(LintWarning("0* represents a 0-22 IV band, not exact 0% IV.", isError = false))
        }
        if (query.contains("Mega", ignoreCase = true)) {
            warnings.add(LintWarning("Shortcut 'Mega' expands to 'Mega0-'. Use exact names.", isError = false))
        }
        return warnings
    }
}
