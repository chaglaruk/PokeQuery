package com.example.pokequery.domain.engine

import com.example.pokequery.data.model.RiskLevel
import com.example.pokequery.domain.lint.Linter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringBuilderEngineTest {

    @Test
    fun `no generated default string contains pipe`() {
        val result = StringBuilderEngine.buildString("test|query", true, "test")
        assertFalse(result.rawSyntax.contains("|"))
        assertTrue(result.rawSyntax.contains(","))
    }

    @Test
    fun `count templates include required exclusions`() {
        val result = StringBuilderEngine.buildString("count2-", false, "test")
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!legendary"))
    }

    @Test
    fun `safe cleanup includes default exclusions`() {
        val result = StringBuilderEngine.buildString("", true, "test")
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!4*"))
        assertTrue(result.rawSyntax.contains("!0*"))
    }

    @Test
    fun `2x candy prep includes count2- and warning`() {
        val result = StringBuilderEngine.buildString("count2-", true, "test")
        assertTrue(result.rawSyntax.contains("count2-"))
        assertEquals(RiskLevel.Medium, result.riskLevel)
    }

    @Test
    fun `linter catches pipe`() {
        val warnings = Linter.lint("shiny|lucky")
        assertTrue(warnings.any { it.message.contains("|") })
    }

    @Test
    fun `linter catches unsafe count`() {
        val warnings = Linter.lint("count3-")
        assertTrue(warnings.any { it.message.contains("Unsafe count usage") })
    }
}
