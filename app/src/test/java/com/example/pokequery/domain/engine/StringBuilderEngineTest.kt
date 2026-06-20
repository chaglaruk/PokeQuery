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
        val result = StringBuilderEngine.buildString(
            baseQuery = "test|query", 
            explanation = "test"
        )
        assertFalse(result.rawSyntax.contains("|"))
        assertTrue(result.rawSyntax.contains(","))
    }

    @Test
    fun `count templates include required exclusions`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "count2-", 
            protections = emptyList(), // no optional protections
            explanation = "test"
        )
        // mandatory protections for count should still apply
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!legendary"))
    }

    @Test
    fun `safe cleanup includes default exclusions`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "1*", 
            explanation = "test"
        )
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!4*"))
        assertFalse(result.rawSyntax.contains("!0*")) // 0* is not in DEFAULT_PROTECTIONS anymore based on new spec
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
    fun `linter catches pipe`() {
        val warnings = Linter.lint("shiny|lucky")
        assertTrue(warnings.any { it.message.contains("|") })
    }

    @Test
    fun `linter catches unsafe count`() {
        val warnings = Linter.lint("count3-")
        assertTrue(warnings.any { it.message.contains("Unsafe count usage") })
    }

    @Test
    fun `engine adds count warning`() {
        val result = StringBuilderEngine.buildString(baseQuery = "count2-", explanation = "test")
        assertTrue(result.warnings.any { it.contains("Count is based on Pokédex species number") })
    }

    @Test
    fun `engine adds trade disclaimer`() {
        val result = StringBuilderEngine.buildString(baseQuery = "trade", explanation = "test")
        assertTrue(result.warnings.any { it.contains("Real trade eligibility depends on friendship level") })
    }

    @Test
    fun `safe cleanup includes positive condition and review explanation`() {
        // Simulating Navigation.kt pass
        val explanation = "This is a REVIEW string targeting 1-star low-value candidates. It is not an automatic transfer command."
        val result = StringBuilderEngine.buildString(baseQuery = "1*", explanation = explanation)
        assertTrue(result.rawSyntax.contains("1*"))
        assertTrue(result.plainLanguageExplanation.contains("REVIEW string"))
        assertTrue(result.plainLanguageExplanation.contains("not an automatic transfer"))
    }
}
