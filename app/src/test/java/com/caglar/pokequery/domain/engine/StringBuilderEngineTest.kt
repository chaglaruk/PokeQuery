package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.lint.Linter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringBuilderEngineTest {
    @Test
    fun `hundo check produces 4 star`() {
        val result = StringBuilderEngine.buildGoal("hundo_check")
        assertEquals("4*", result.rawSyntax)
        assertEquals(RiskLevel.Info, result.riskLevel)
    }

    @Test
    fun `nundo finder produces exact zero iv`() {
        val result = StringBuilderEngine.buildGoal("nundo_finder")
        assertEquals("0attack&0defense&0hp", result.rawSyntax)
        assertEquals(RiskLevel.Info, result.riskLevel)
    }

    @Test
    fun `pvp candidates default to great league`() {
        val result = StringBuilderEngine.buildGoal("pvp_candidates")
        assertEquals("0-1attack&3-4defense&3-4hp&cp-1500", result.rawSyntax)
        assertEquals(RiskLevel.Info, result.riskLevel)
    }

    @Test
    fun `pvp candidates ultra mode uses 2500 cap`() {
        val result = StringBuilderEngine.buildGoal("pvp_candidates", config = "ultra")
        assertEquals("0-1attack&3-4defense&3-4hp&cp-2500", result.rawSyntax)
    }

    @Test
    fun `lucky trade age is action adjacent but does not add cleanup exclusions`() {
        val result = StringBuilderEngine.buildGoal("lucky_trade", config = "age")
        assertEquals("age365-&!traded", result.rawSyntax)
        assertEquals(RiskLevel.Medium, result.riskLevel)
    }

    @Test
    fun `lucky trade distance is action adjacent but does not add cleanup exclusions`() {
        val result = StringBuilderEngine.buildGoal("lucky_trade", config = "distance")
        assertEquals("distance100-&!traded", result.rawSyntax)
        assertEquals(RiskLevel.Medium, result.riskLevel)
    }

    @Test
    fun `safe cleanup includes default exclusions`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "1*",
            explanation = "test"
        )
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!4*"))
        assertFalse(result.rawSyntax.contains("!0*"))
    }

    @Test
    fun `2x candy prep includes count2- and warning`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "count2-",
            explanation = "test"
        )
        assertTrue(result.rawSyntax.contains("count2-"))
        assertEquals(RiskLevel.Medium, result.riskLevel)
    }

    @Test
    fun `linter blocks pipe operator even if game client documents it`() {
        val warnings = Linter.lint("shiny|lucky")
        assertTrue(warnings.any { it.isError && it.message.contains("|") })
    }

    @Test
    fun `linter catches unsafe count`() {
        val warnings = Linter.lint("count3-")
        assertTrue(warnings.any { it.message.contains("Unsafe count usage") })
        assertTrue(warnings.any { it.message.contains("!costume") })
    }

    @Test
    fun `linter catches count shortcut risky inclusion and reserved tag collision`() {
        val warnings = Linter.lint("count&shiny&#shiny")
        assertTrue(warnings.any { it.message.contains("count2-") })
        assertTrue(warnings.any { it.message.contains("Risky inclusion of shiny") })
        assertTrue(warnings.any { it.message.contains("collides") })
    }

    @Test
    fun `engine adds count warning`() {
        val result = StringBuilderEngine.buildString(baseQuery = "count2-", explanation = "test")
        assertTrue(result.warnings.any { it.contains("Count is based on Pokédex species number") })
    }

    @Test
    fun `engine normalizes pipe before generated output`() {
        val result = StringBuilderEngine.buildString(baseQuery = "shiny|legendary", protections = emptyList(), explanation = "test")
        assertEquals("shiny,legendary", result.rawSyntax)
        assertFalse(result.rawSyntax.contains("|"))
    }
}
