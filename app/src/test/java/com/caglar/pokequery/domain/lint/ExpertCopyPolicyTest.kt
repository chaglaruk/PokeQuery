package com.caglar.pokequery.domain.lint

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the v0.4.2 safety patch (Fix 2).
 *
 * Audit finding (BUG-005): ExpertBuilder displayed linter errors but still allowed
 * copy/generate. Error-level warnings must block copy; advisory warnings may proceed.
 */
class ExpertCopyPolicyTest {

    @Test
    fun `pipe operator blocks copy`() {
        // '|' is an error-level linter warning.
        assertFalse(ExpertCopyPolicy.canCopy("shiny|lucky"))
    }

    @Test
    fun `unsafe bare count blocks copy`() {
        // Bare 'count' (without the mandatory exclusions) is an error-level warning.
        assertFalse(ExpertCopyPolicy.canCopy("count"))
    }

    @Test
    fun `empty query does not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy(""))
    }

    @Test
    fun `advisory-only warnings do not block copy`() {
        // '0*' alone produces only an advisory (0* is an IV band, not exact 0% IV) — no error.
        assertTrue(ExpertCopyPolicy.canCopy("0*"))
    }

    @Test
    fun `clean safe query does not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("4*&!shiny"))
    }

    // v0.5.1 (Fix 7): lucky + traded is an advisory positive filter, NOT a cleanup/count
    // search. The previous `"trade" in lower` substring matched the 'traded' token and
    // fail-closed copy. Copy must stay enabled with a visible advisory.
    @Test
    fun `lucky and traded positive filters do not block copy`() {
        assertTrue(ExpertCopyPolicy.canCopy("lucky,traded"))
        assertTrue(ExpertCopyPolicy.canCopy("lucky&traded"))
    }

    @Test
    fun `advisory risky positive filter does not block copy`() {
        // shiny as a positive filter outside a cleanup/count context is advisory only.
        assertTrue(ExpertCopyPolicy.canCopy("shiny"))
        assertTrue(ExpertCopyPolicy.canCopy("legendary"))
    }

    @Test
    fun `true error still blocks copy after fix 7`() {
        // pipe is a true error.
        assertFalse(ExpertCopyPolicy.canCopy("shiny|lucky"))
        // bare count without mandatory exclusions is a true error.
        assertFalse(ExpertCopyPolicy.canCopy("count2-"))
        // risky inclusion in a count context is still a true error.
        assertFalse(ExpertCopyPolicy.canCopy("count2-&shiny"))
    }
}
