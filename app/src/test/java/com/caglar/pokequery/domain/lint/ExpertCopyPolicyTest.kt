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
}
