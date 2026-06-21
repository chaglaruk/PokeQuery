package com.caglar.pokequery.domain.lint

/**
 * v0.4.2 safety patch (Fix 2, audit BUG-005).
 *
 * Expert Builder must not copy/generate when the linter reports any error-level
 * warning. Advisory warnings (e.g. "0* is an IV band") may still proceed.
 *
 * Centralizing this rule here keeps it unit-testable independently of the Compose UI.
 */
object ExpertCopyPolicy {

    fun canCopy(rawQuery: String): Boolean =
        !Linter.lint(rawQuery).any { it.isError }
}
