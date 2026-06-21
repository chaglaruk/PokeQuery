package com.caglar.pokequery.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the v0.4.2 safety patch (Fix 1).
 *
 * Audit finding (BUG-001): GoalDetailScreen re-derived the base query by splitting
 * baseGoal.rawSyntax on '&' and keeping only the first non-exclusion token, which
 * silently dropped the engine-provided '!traded' term from trade_fodder / lucky_trade.
 *
 * These tests pin the invariant: trade/lucky goals always keep their engine-mandated
 * terms, optional protections are additive only, and no duplicate '!traded' is produced.
 */
class GoalStringBuilderTest {

    private fun baseGoal(goalId: String, config: String = "", language: String = "English") =
        StringBuilderEngine.buildGoal(goalId, config, language)

    @Test
    fun `trade_fodder final string always contains !traded`() {
        val result = GoalStringBuilder.buildFinal(baseGoal("trade_fodder"), optionalProtections = emptyList())
        assertTrue("Expected '!traded' in: ${result.rawSyntax}", result.rawSyntax.contains("!traded"))
    }

    @Test
    fun `lucky_trade final string always contains !traded`() {
        val result = GoalStringBuilder.buildFinal(baseGoal("lucky_trade", config = "age"), optionalProtections = emptyList())
        assertTrue("Expected '!traded' in: ${result.rawSyntax}", result.rawSyntax.contains("!traded"))
    }

    @Test
    fun `optional protections never remove engine-mandated terms`() {
        val result = GoalStringBuilder.buildFinal(baseGoal("trade_fodder"), optionalProtections = emptyList())
        assertTrue(result.rawSyntax.contains("!traded"))
        assertTrue(result.rawSyntax.contains("count2-"))
    }

    @Test
    fun `optional protections extend a trade goal without dropping !traded`() {
        val result = GoalStringBuilder.buildFinal(
            baseGoal("trade_fodder"),
            optionalProtections = listOf("shiny", "legendary")
        )
        assertTrue("Expected '!traded' retained: ${result.rawSyntax}", result.rawSyntax.contains("!traded"))
        assertTrue("Expected '!shiny' added: ${result.rawSyntax}", result.rawSyntax.contains("!shiny"))
        assertTrue("Expected '!legendary' added: ${result.rawSyntax}", result.rawSyntax.contains("!legendary"))
    }

    @Test
    fun `no duplicate !traded is produced for trade goals`() {
        // 'traded' supplied both as an optional protection and engine-mandated must not double up.
        val result = GoalStringBuilder.buildFinal(
            baseGoal("trade_fodder"),
            optionalProtections = listOf("traded")
        )
        assertFalse("Duplicate '!traded&!traded' found: ${result.rawSyntax}", result.rawSyntax.contains("!traded&!traded"))
        assertTrue(result.rawSyntax.contains("!traded"))
    }

    @Test
    fun `safe_cleanup keeps its positive condition and added protections`() {
        val result = GoalStringBuilder.buildFinal(
            baseGoal("safe_cleanup"),
            optionalProtections = listOf("shiny", "4*")
        )
        assertTrue("Expected '1*' retained: ${result.rawSyntax}", result.rawSyntax.contains("1*"))
        assertTrue("Expected '!shiny': ${result.rawSyntax}", result.rawSyntax.contains("!shiny"))
        assertTrue("Expected '!4*': ${result.rawSyntax}", result.rawSyntax.contains("!4*"))
    }

    @Test
    fun `hundo_check passes through unchanged`() {
        val result = GoalStringBuilder.buildFinal(baseGoal("hundo_check"), optionalProtections = emptyList())
        assertEquals("4*", result.rawSyntax)
    }

    @Test
    fun `pvp_candidates passes through unchanged`() {
        val result = GoalStringBuilder.buildFinal(baseGoal("pvp_candidates", config = "great"), optionalProtections = emptyList())
        assertEquals("0-1attack&3-4defense&3-4hp&cp-1500", result.rawSyntax)
    }
}
