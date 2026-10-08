package com.caglar.pokequery.domain.lint

/** Reference templates are educational text, not ready-to-paste search tokens. */
object KnowledgeCopyPolicy {
    fun canCopy(syntax: String): Boolean =
        syntax.isNotBlank() && syntax.none { it in "[]<>|" }
}
