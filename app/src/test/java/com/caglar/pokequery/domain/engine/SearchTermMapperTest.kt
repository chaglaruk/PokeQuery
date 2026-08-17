package com.caglar.pokequery.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for search-string language selection and localized token emission.
 */
class SearchTermMapperTest {

    @Test
    fun `English is the safe default`() {
        assertEquals("English", SearchTermMapper.resolveLanguage("English"))
    }

    @Test
    fun `Auto resolves to English regardless of device locale`() {
        assertEquals("English", SearchTermMapper.resolveLanguage("Auto"))
        assertEquals("English", SearchTermMapper.resolveLanguage(""))
        assertEquals("English", SearchTermMapper.resolveLanguage("   "))
    }

    @Test
    fun `explicit Turkish selection is preserved`() {
        assertEquals("Turkish", SearchTermMapper.resolveLanguage("Turkish"))
    }

    @Test
    fun `English pass-through does not translate tokens`() {
        val result = SearchTermMapper.translateSyntax("count2-&!shiny&!traded", "English")
        assertEquals("count2-&!shiny&!traded", result)
    }

    @Test
    fun `explicit Turkish selection still translates according to the current map`() {
        val result = SearchTermMapper.translateSyntax("count2-&!shiny", "Turkish")
        assertTrue("count must stay English (fallback) in Turkish output: $result", result.contains("count2-"))
        assertFalse("count must NOT be translated to 'toplam': $result", result.contains("toplam"))
        assertTrue("Expected Turkish shiny token in: $result", result.contains("parlak"))
    }

    @Test
    fun `Auto no longer emits Turkish tokens even when a token is translatable`() {
        val result = SearchTermMapper.translateSyntax("shiny&!traded", "Auto")
        assertFalse("Auto must not translate 'shiny' to 'parlak': $result", result.contains("parlak"))
        assertEquals("shiny&!traded", result)
    }

    @Test
    fun `looksTurkish detects translated output for the risk-warning beta notice`() {
        assertTrue(SearchTermMapper.looksTurkish("count2-&!parlak"))
        assertTrue(SearchTermMapper.looksTurkish("count2-&!gölge"))
        assertFalse(SearchTermMapper.looksTurkish("count2-&!shiny"))
        assertFalse(SearchTermMapper.looksTurkish("4*"))
    }

    @Test
    fun `Turkish count cleanup translates only officially documented safe tokens`() {
        val base = "count2-&!shiny&!lucky&!legendary&!mythical&!shadow&!purified&!favorite&" +
            "!traded&!costume&!ultrabeast&!background&!locationbackground&!specialbackground&!#&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        assertTrue("count must stay English (fallback): $result", result.contains("count2-"))
        assertFalse("must not emit 'toplam': $result", result.contains("toplam"))
        assertTrue("!traded must stay English fallback: $result", result.contains("!traded"))
        assertTrue("!specialbackground must stay English fallback: $result", result.contains("!specialbackground"))
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("legendary -> efsanevi expected: $result", result.contains("!efsanevi"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("ultrabeast -> ultracanavar expected: $result", result.contains("!ultracanavar"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
        assertTrue("locationbackground -> konumarkaplanı expected: $result", result.contains("!konumarkaplanı"))
    }

    @Test
    fun `Turkish safe cleanup keeps only unresolved protection terms in English fallback`() {
        val base = "1*&!shiny&!legendary&!mythical&!ultrabeast&!costume&!background&" +
            "!locationbackground&!specialbackground&!shadow&!purified&!favorite&!lucky&!#&!traded&!4*"
        val result = SearchTermMapper.translateSyntax(base, "Turkish")

        assertTrue("!specialbackground stays English: $result", result.contains("!specialbackground"))
        assertTrue("!traded stays English: $result", result.contains("!traded"))
        assertTrue("shiny -> parlak expected: $result", result.contains("!parlak"))
        assertTrue("costume -> kostüm expected: $result", result.contains("!kostüm"))
        assertTrue("background -> arkaplan expected: $result", result.contains("!arkaplan"))
    }

    @Test
    fun `operators remain canonical while tokens translate`() {
        val result = SearchTermMapper.translateSyntax("shiny,!legendary&cp1500-", "Turkish")
        assertEquals("parlak,!efsanevi&dg1500-", result)
    }

    @Test
    fun `unverified tokens are reported for localized output`() {
        val unverified = SearchTermMapper.findUnverifiedTokens("count2-&!traded&!specialbackground", "Turkish")
        assertEquals(listOf("count", "traded", "specialbackground"), unverified)
    }

    @Test
    fun `German lucky uses the official Gluecks search token`() {
        assertEquals("glücks", SearchTermMapper.translateSyntax("lucky", "German"))
        assertEquals("!glücks", SearchTermMapper.translateSyntax("!lucky", "German"))
    }

    @Test
    fun `Spanish lucky uses the official multi-word con suerte token`() {
        assertEquals("con suerte", SearchTermMapper.translateSyntax("lucky", "Spanish"))
        assertEquals("!con suerte", SearchTermMapper.translateSyntax("!lucky", "Spanish"))
    }

    @Test
    fun `Spanish traded uses the official intercambiados token`() {
        assertEquals("intercambiados", SearchTermMapper.translateSyntax("traded", "Spanish"))
        assertEquals("!intercambiados", SearchTermMapper.translateSyntax("!traded", "Spanish"))
    }

    @Test
    fun `Turkish official CP and HP abbreviations remain DG and SP`() {
        assertEquals("dg300&sp150", SearchTermMapper.translateSyntax("cp300&hp150", "Turkish"))
    }
}
