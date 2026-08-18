package com.caglar.pokequery.domain.assist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostRelease075RegressionTest {

    @Test
    fun `keyword matching rejects accidental substrings`() {
        val accidentalInputs = listOf(
            "traded",
            "check my storage",
            "small pokemon",
            "extraordinary",
            "eventually",
            "undelete",
            "junkyard",
            "footage"
        )

        accidentalInputs.forEach { input ->
            val result = SearchIntentParser.parse(input, LocalDate(2026, 8, 18))
            assertFalse("$input must not build a query", result.canBuild)
            assertEquals("$input must not emit syntax", "", result.rawQuery)
        }
    }

    @Test
    fun `punctuation-bearing exact keywords still work`() {
        assertEquals("4*", SearchIntentParser.parse("100%", LocalDate(2026, 8, 18)).rawQuery)
        assertEquals("4*", SearchIntentParser.parse("15/15/15", LocalDate(2026, 8, 18)).rawQuery)
    }

    @Test
    fun `pipe input is rejected instead of changing OR into AND`() {
        val result = SearchIntentParser.parse("shiny|lucky", LocalDate(2026, 8, 18))
        assertFalse(result.canBuild)
        assertTrue(result.tokens.isEmpty())
        assertTrue(result.exclusions.isEmpty())
        assertEquals("", result.rawQuery)
    }

    @Test
    fun `modal may is not treated as the month May`() {
        val result = SearchIntentParser.parse("caught anything I may, or may not, want", LocalDate(2026, 8, 18))
        assertFalse(result.canBuild)
        assertEquals("", result.rawQuery)

        val realMonth = SearchIntentParser.parse("caught in May 2026", LocalDate(2026, 8, 18))
        assertTrue(realMonth.canBuild)
        assertEquals("year2026&age79-109", realMonth.rawQuery)
    }

    @Test
    fun `smart apostrophe preserves inverted negation`() {
        val result = SearchIntentParser.parse("don’t hide shiny", LocalDate(2026, 8, 18))
        assertTrue(result.canBuild)
        assertEquals("shiny", result.rawQuery)
    }

    @Test
    fun `bare not stops at conjunction boundary`() {
        val result = SearchIntentParser.parse("not shiny and legendary", LocalDate(2026, 8, 18))
        assertTrue(result.canBuild)
        assertTrue("shiny must be excluded", "shiny" in result.exclusions)
        assertTrue("legendary must stay positive", "legendary" in result.tokens)
        assertFalse("legendary must not inherit bare not", "legendary" in result.exclusions)
        assertEquals("legendary&!shiny", result.rawQuery)
    }

    @Test
    fun `turkish lucky and purified aliases emit canonical tokens only`() {
        val lucky = SearchIntentParser.parse("şanslı", LocalDate(2026, 8, 18))
        val purified = SearchIntentParser.parse("arınmış", LocalDate(2026, 8, 18))

        assertEquals(listOf("lucky"), lucky.tokens)
        assertEquals("lucky", lucky.rawQuery)
        assertEquals(listOf("purified"), purified.tokens)
        assertEquals("purified", purified.rawQuery)
    }

    @Test
    fun `affirmative tag words no longer mean untagged`() {
        listOf("tagged", "tag", "etiket").forEach { input ->
            val result = SearchIntentParser.parse(input, LocalDate(2026, 8, 18))
            assertFalse("$input must not silently invert to !#", result.canBuild)
            assertEquals("", result.rawQuery)
        }

        assertEquals("!#", SearchIntentParser.parse("untagged", LocalDate(2026, 8, 18)).rawQuery)
        assertEquals("!#", SearchIntentParser.parse("etiketsiz", LocalDate(2026, 8, 18)).rawQuery)
    }
}
